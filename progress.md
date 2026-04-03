# 进度日志

## 2026-04-03

### 会话 1

**任务**: 创建任务计划文件

**操作**:
1. 读取 TODO.md，了解优化改造点
2. 创建 task_plan.md，包含 9 个阶段的详细计划
3. 创建 findings.md，记录研究发现
4. 创建 progress.md，初始化进度日志

**状态**: 计划文件创建完成，待用户确认后开始执行

---

### 会话 2

**任务**: 执行阶段 1 - 安全加固 (P1)

**操作**:

#### 1.1 密码恒定时间比对
- **文件**: `springdoc-plus-gateway-spring-boot-starter/.../BasicAuthWebFilter.java`
- **修改**: 将 `expected.equals(decoded)` 改为 `MessageDigest.isEqual()`
- **理由**: 防止时序攻击，恒定时间比对

```java
// 修改前
if (!expected.equals(decoded)) { return unauthorized(exchange); }

// 修改后
boolean matches = MessageDigest.isEqual(
    expected.getBytes(StandardCharsets.UTF_8),
    decoded.getBytes(StandardCharsets.UTF_8)
);
if (!matches) { return unauthorized(exchange); }
```

#### 1.2 静态资源路径遍历防护
- **文件**: `springdoc-plus-gateway-spring-boot-starter/.../DocHtmlController.java`
- **修改**: 在 `uiAsset()` 和 `uiRootAsset()` 方法中添加 `isValidFilename()` 校验
- **理由**: 防止路径遍历攻击

```java
private boolean isValidFilename(String filename) {
    return filename != null
            && !filename.contains("..")
            && !filename.contains("/")
            && !filename.contains("\\");
}
```

**状态**: 阶段 1 完成

**验证方式**:
- 构建项目确认编译通过: `mvn -q -DskipTests package`
- 手动测试路径遍历 Payload（如 `../../../application.yml`）

---

### 会话 3

**任务**: 执行阶段 6 - 测试覆盖 (P1)

**操作**:

#### 6.1 添加测试依赖
- **文件**: `springdoc-plus-gateway-spring-boot-starter/pom.xml`
- **修改**: 添加 `spring-boot-starter-test` 和 `reactor-test` 测试依赖

#### 6.2 创建单元测试类
- **GatewayRouteDefinitionResolverTest.java**:
  - 测试 lb:// 服务解析
  - 测试非 lb:// 方案过滤
  - 测试 StripPrefix 过滤器
  - 测试多路径模式（逗号分隔）
  - 测试空路由列表

- **DiscoverGroupsServiceTest.java**:
  - 测试 MANUAL 模式返回配置路由
  - 测试 DISCOVER 模式从服务发现生成路由
  - 测试排除指定服务（精确匹配）
  - 测试正则表达式排除
  - 测试 ServiceConfig 个性化配置
  - 测试排序按 order 字段
  - 测试空服务列表和 null routes 保护

- **BasicAuthWebFilterTest.java**:
  - 测试未启用/启用 Basic 认证
  - 测试无需保护的路径
  - 测试无/无效/正确 Authorization header
  - 测试正确/错误凭据
  - 测试空用户名/密码/null 值
  - 测试各保护路径（/doc.html, /springdoc-plus-ui/**, /springdoc-plus-gateway/**）

**状态**: 阶段 6 完成

**验证方式**:
- 运行测试: `mvn test`（需要 Java 21 环境）

---

### 待处理任务

按优先级顺序：

1. **P2 - 性能优化** - 缓存 /groups 接口 + 异步改造
2. **P2 - 代码重复消除** - 统一 tagGroups
3. **P2 - 错误处理** - 统一异常处理 + 日志
4. **P2 - 前端工程** - YAML 支持
5. **P3 - 扩展性** - 单服务配置化
6. **P3 - 可观测性** - SLF4J 日志
7. **P3 - 构建流程** - 前端后端解耦