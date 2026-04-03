# 研究与发现

## TODO.md 任务清单

### P1 必做项

1. **安全加固**
   - `BasicAuthWebFilter.java`: 密码明文比对，时序攻击风险
   - `DocHtmlController.java`: 静态资源路径遍历漏洞

2. **测试覆盖**
   - 项目 31 个 Java 文件，零单元测试
   - 建议覆盖: DiscoverGroupsService, GatewayRouteDefinitionResolver, BasicAuthWebFilter

### P2 重要项

1. **性能优化**
   - `/groups` 接口每次请求都调用服务发现
   - `.toStream()` 阻塞调用在 WebFlux 中是反模式

2. **代码重复**
   - `App.vue` 的 tagGroups 与 `useOpenApi.ts` 完全重复

3. **错误处理**
   - 多处异常被静默吞没
   - 无统一 @ExceptionHandler

4. **前端工程质量**
   - YAML 格式不支持（明确 throw Error）
   - 状态管理散落在 useConfig

### P3 优化项

1. **扩展性设计**: SingleOpenApiGroupsController 硬编码
2. **可观测性**: 无 SLF4J 日志
3. **构建流程**: 前端后端耦合

---

## 文件位置参考

### 后端 Java 文件
- `springdoc-plus-gateway-spring-boot-starter/.../BasicAuthWebFilter.java`
- `springdoc-plus-openapi3-spring-boot-starter/.../DocHtmlController.java`
- `springdoc-plus-gateway-spring-boot-starter/.../DiscoverGroupsService.java`
- `springdoc-plus-gateway-spring-boot-starter/.../GatewayRouteDefinitionResolver.java`
- `springdoc-plus-openapi3-spring-boot-starter/.../SingleOpenApiGroupsController.java`

### 前端 TypeScript/Vue 文件
- `springdoc-plus-web/src/App.vue`
- `springdoc-plus-web/src/composables/useOpenApi.ts`
- `springdoc-plus-web/src/composables/useConfig.ts`

---

## 依赖版本参考

- Spring Boot: 4.0.1
- Spring Cloud: 2025.1.0
- Springdoc: 3.0.1
- Java: 21
- Vue: 3
- Tailwind CSS: v4

---

## 待研究

- [ ] Caffeine Cache 集成方式
- [ ] js-yaml 轻量级解析
- [ ] WebFlux 统一异常处理最佳实践
- [ ] frontend-maven-plugin 配置
- [ ] Jacoco 覆盖率配置