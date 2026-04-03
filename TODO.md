
## 一、安全加固（P1 必做）

**问题 1：密码明文比对**
`BasicAuthWebFilter.java` 中直接用字符串 `equals` 对比明文密码，且比对时间不恒定，存在时序攻击风险。

```java
// 当前：明文 equals，有时序攻击风险
if (!expected.equals(decoded)) { return unauthorized(exchange); }

// 建议：改用 MessageDigest 恒定时间比对
MessageDigest.isEqual(expected.getBytes(), decoded.getBytes())
```

**问题 2：静态资源路径遍历**
`DocHtmlController.java` 中 `@PathVariable String filename` 直接拼入 classpath 路径，未做任何校验，理论上可构造 `../../../application.yml` 形式的路径。

```java
// 建议在方法开头加白名单校验
if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
    return Mono.just(ResponseEntity.badRequest().build());
}
```

---

## 二、性能优化（P2 重要）

**问题：`/groups` 接口每次请求都重新调用服务发现和路由解析**

`DiscoverGroupsService.getGroups()` 每次请求都调用 `discoveryClient.getServices()` 和 Reactor 的 `toStream()`（阻塞调用在 WebFlux 中是反模式），路由表通常是秒级/分钟级变化的，完全可以缓存。

```java
// 建议：加 TTL 缓存（Caffeine 或 Spring Cache）
@Cacheable(value = "gateway-groups", unless = "#result.isEmpty()")
public List<GatewayRoute> getGroups(Optional<List<String>> ids) { ... }

// 同时将 .toStream() 改为异步方式处理
```

另外 `routeDefinitionLocator.getRouteDefinitions()` 是 `Flux`，调用 `.toStream()` 是阻塞操作，建议返回 `Mono<List<GatewayRoute>>` 走全异步链路。

---

## 三、代码重复消除（P2 重要）

`App.vue` 中的 `tagGroups` computed 与 `useOpenApi.ts` 中的 `tagGroups` computed 是完全重复的实现（路径遍历、排序逻辑一模一样）。`App.vue` 应该直接复用 `useOpenApi` 这个 composable，而不是自己再实现一遍。

```typescript
// App.vue 删掉本地的 tagGroups computed，改为
const { spec, tagGroups, loading, load } = useOpenApi(configStore.state)
```

---

## 四、错误处理（P2 重要）

多处异常被吞没且无日志：

- `GatewayRouteDefinitionResolver.toResolved()` 对解析失败静默返回 `null`
- `excluded()` 方法中 `catch (Exception ignore)` 完全无声
- `buildBodyFromParams()` 中 JSON 解析失败也是静默降级

建议引入统一的 `@ExceptionHandler`（WebFlux 用 `@ControllerAdvice`），并在关键路径上加结构化日志。

---

## 五、前端工程质量（P2 重要）

**YAML 格式支持缺失**

`useOpenApi.ts` 中明确写了 `throw new Error('暂不支持 YAML 格式')`，而 SpringDoc 默认同时暴露 JSON 和 YAML 端点，很多用户会碰到这个限制。可引入 `js-yaml`（轻量级，约 20KB）：

```typescript
import yaml from 'js-yaml'
data = yaml.load(await res.text()) as OpenApiSpec
```

**前端状态管理散落**

全局状态（auth 配置、sorter 配置）散落在 `useConfig` composable 中，使用 `localStorage` 直接持久化。随着功能增长，建议引入 Pinia 做统一状态管理，并把持久化逻辑抽象成 plugin。

---

## 六、测试覆盖（P1 必做）

整个项目 31 个 Java 文件中**零单元测试**，也无集成测试目录。作为一个发布到 Maven Central 的开源库，这是最高风险项。

建议优先补齐：
- `DiscoverGroupsService` 的分组逻辑（manual/discover/混合模式）
- `GatewayRouteDefinitionResolver` 的路径推断逻辑（各种 StripPrefix 场景）
- `BasicAuthWebFilter` 的鉴权流程

可用 `WebFluxTest` + `StepVerifier` 快速覆盖响应式链路。

---

## 七、扩展性设计（P3 优化）

`SingleOpenApiGroupsController` 中将分组信息全部硬编码：

```java
// 当前：硬编码 url、contextPath、name
Map.of("name", "default", "url", "/v3/api-docs", ...)
```

应参照 gateway 侧的 `SpringdocPlusGatewayProperties`，为单服务场景也提供配置项（自定义分组名、docs URL、认证配置等），复用同一套 UI 配置接口，而不是硬编码死。

---

## 八、可观测性（P3 优化）

整个后端没有任何 SLF4J 日志调用，出问题完全无从排查。建议在以下位置加日志：

- 服务发现获取服务列表时（DEBUG 级别，输出 serviceId 列表）
- 路由推断失败时（WARN）
- Basic Auth 鉴权失败时（INFO，注意不要打印密码）

另外可考虑暴露 `/actuator/health` 自定义指示器，反映 gateway 聚合文档是否正常。

---

## 九、构建流程（P3 优化）

**前端与后端耦合**：`package.json` 的 `deploy` 脚本用 Node.js 脚本手动把 `dist/` 复制到 Maven 模块目录，这是 monorepo 的脆弱点，前端一 build 就会改变已跟踪的 Java 资源文件。

建议用 `frontend-maven-plugin` 把前端构建纳入 Maven 生命周期，或者用 GitHub Actions 在 CI 里统一构建 + 复制，确保发布物的一致性。目前 `pom.xml` 配置了 Jacoco 但没有设置覆盖率阈值门禁，建议加上 `<rule><minimum>0.60</minimum></rule>` 作为合并保护。