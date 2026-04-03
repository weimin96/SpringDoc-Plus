# 任务计划：SpringDoc-Plus 项目优化改造

## 目标
对 TODO.md 中列出的 9 个优化点进行系统性改造，提升项目的安全性、性能、可维护性和可测试性。

---

## 阶段概览

| 阶段 | 类型 | 优先级 | 描述 |
|------|------|--------|------|
| 1 | 安全加固 | P1 | 密码恒定时间比对 + 路径遍历防护 |
| 2 | 性能优化 | P2 | 缓存 /groups 接口 + 异步改造 |
| 3 | 代码重复消除 | P2 | 统一 tagGroups 实现 |
| 4 | 错误处理 | P2 | 统一异常处理 + 结构化日志 |
| 5 | 前端工程质量 | P2 | YAML 支持 + 状态管理 |
| 6 | 测试覆盖 | P1 | 单元测试补齐 |
| 7 | 扩展性设计 | P3 | 单服务配置化 |
| 8 | 可观测性 | P3 | SLF4J 日志 |
| 9 | 构建流程 | P3 | 前端后端解耦 |

---

## 阶段 1：安全加固（P1 必做）

### 1.1 密码恒定时间比对
- **文件**: `springdoc-plus-gateway-spring-boot-starter/.../BasicAuthWebFilter.java`
- **问题**: 明文 `equals` 比对，存在时序攻击风险
- **修改**: 改用 `MessageDigest.isEqual()` 恒定时间比对

### 1.2 静态资源路径遍历防护
- **文件**: `springdoc-plus-openapi3-spring-boot-starter/.../DocHtmlController.java`
- **问题**: `@PathVariable` 未校验，可构造路径遍历
- **修改**: 添加白名单校验，禁止 `..` `/` `\`

### 验证方式
- 单元测试覆盖 BasicAuthWebFilter 鉴权逻辑
- 手动测试路径遍历 Payload

---

## 阶段 2：性能优化（P2 重要）

### 2.1 缓存 /groups 接口
- **文件**: `DiscoverGroupsService.java`
- **问题**: 每次请求都调用服务发现和路由解析
- **修改**: 引入 Caffeine Cache，设置 TTL

### 2.2 异步链路改造
- **问题**: `.toStream()` 是阻塞操作
- **修改**: 返回 `Mono<List<GatewayRoute>>` 全异步

### 验证方式
- 使用 JMeter 或 wrk 压测 `/groups` 接口，对比改造前后 QPS

---

## 阶段 3：代码重复消除（P2 重要）

### 3.1 统一 tagGroups 实现
- **文件**: `App.vue`, `useOpenApi.ts`
- **问题**: tagGroups 在两处重复实现
- **修改**: App.vue 复用 useOpenApi composable

### 验证方式
- 页面功能测试，确保 UI 行为一致

---

## 阶段 4：错误处理（P2 重要）

### 4.1 统一异常处理
- **修改**: 引入 `@ControllerAdvice` + `@ExceptionHandler`

### 4.2 结构化日志
- **位置**:
  - 服务发现获取列表（DEBUG）
  - 路由推断失败（WARN）
  - Basic Auth 失败（INFO）

### 验证方式
- 人为触发异常，验证日志输出和错误响应

---

## 阶段 5：前端工程质量（P2 重要）

### 5.1 YAML 格式支持
- **文件**: `useOpenApi.ts`
- **修改**: 引入 `js-yaml` 库，支持 YAML 解析

### 5.2 状态管理（可选）
- 评估是否引入 Pinia

### 验证方式
- 使用 YAML 格式的 OpenAPI 文档测试

---

## 阶段 6：测试覆盖（P1 必做）

### 6.1 补齐单元测试
- `DiscoverGroupsService` 分组逻辑（manual/discover/混合）
- `GatewayRouteDefinitionResolver` 路径推断
- `BasicAuthWebFilter` 鉴权流程

### 验证方式
- 测试覆盖率 >= 60%（Jacoco）

---

## 阶段 7：扩展性设计（P3 优化）

### 7.1 单服务配置化
- **文件**: `SingleOpenApiGroupsController.java`
- **修改**: 参照 GatewayProperties 提供配置项

### 验证方式
- 配置自定义分组名、URL、认证

---

## 阶段 8：可观测性（P3 优化）

### 8.1 SLF4J 日志
- 按阶段 4.2 所列位置添加日志

### 8.2 Actuator 健康检查
- 自定义健康指示器

### 验证方式
- 触发关键路径，验证日志输出

---

## 阶段 9：构建流程（P3 优化）

### 9.1 前端后端解耦
- 评估 `frontend-maven-plugin` 方案
- 或使用 GitHub Actions CI

### 9.2 Jacoco 覆盖率门禁
- 添加 `<rule><minimum>0.60</minimum></rule>`

---

## 当前状态

- [x] 阶段 1: 安全加固 (P1) - 完成
- [x] 阶段 2: 性能优化 (P2) - 完成
- [x] 阶段 3: 代码重复消除 (P2) - 完成
- [x] 阶段 4: 错误处理 (P2) - 完成
- [x] 阶段 5: 前端工程质量 (P2) - 完成
- [x] 阶段 6: 测试覆盖 (P1) - 完成
- [x] 阶段 7: 扩展性设计 (P3) - 完成
- [x] 阶段 8: 可观测性 (P3) - 完成
- [x] 阶段 9: 构建流程 (P3) - 完成

---

## 决策记录

| 日期 | 决策 | 理由 |
|------|------|------|
| 2026-04-03 | 优先处理 P1 安全和测试 | 最高风险项 |
| 2026-04-03 | YAML 库选择 js-yaml | 轻量级，约 20KB |
| 2026-04-03 | 缓存方案选 Caffeine | Spring 生态集成良好 |

---

## Errors Encountered

| Error | Attempt | Resolution |
|-------|---------|------------|
| - | - | - |