# springdoc-plus-ui

基于 Vue 3 + TypeScript + Tailwind CSS v4 + Vite 7 构建的 API 文档 UI。

**无任何第三方运行时依赖**（仅 `vue`）— 自实现 OpenAPI 解析与渲染，不依赖 swagger-ui-dist。

## 特性

| 特性 | 说明 |
|------|------|
| 零外部依赖渲染 | 自实现 OpenAPI 3.x 解析、Tag/Operation 分组、Schema 展示 |
| 统一网关契约 | 前端统一读取 `/springdoc-plus-gateway/openapi/groups` 和 `/springdoc-plus-gateway/ui-config`，单服务 starter 也提供同名接口 |
| Tailwind v4 | 组件样式全部用 Tailwind utility class，`@theme` 注册 design token |
| 代码规范 | Prettier + Stylelint，统一格式化 |
| 接口过滤 | 顶部搜索栏，实时过滤路径/描述/operationId |
| Schema 展示 | 支持 `$ref` 解引用、嵌套对象、数组类型展示 |

## 项目结构

```
src/
├── App.vue                            # 顶层编排
├── main.ts
├── styles/
│   ├── index.css                      # Tailwind @import 入口
│   ├── tokens.css                     # @theme design tokens
│   └── base.css                       # Reset + keyframes
├── types/
│   ├── index.ts                       # 通用类型（Mode, ApiGroup, Config…）
│   └── openapi.ts                     # OpenAPI 3.x spec 类型定义
├── composables/
│   ├── useApi.ts                      # 文档组和服务端 UI 配置获取
│   ├── useConfig.ts                   # localStorage 配置管理
│   ├── useOpenApi.ts                  # OpenAPI spec 解析 + tag/op 分组
│   └── useSwagger.ts                  # 预留 stub
└── components/
    ├── AppTopbar.vue                  # 顶栏
    ├── AppSidebar.vue                 # 侧边栏
    ├── ContentArea.vue                # 主内容区（自实现文档渲染）
    ├── SettingsModal.vue              # 配置弹窗
    └── swagger/
        ├── MethodBadge.vue            # HTTP method 色块
        ├── TagSection.vue             # Tag 折叠区
        ├── OperationPanel.vue         # 单个接口展开面板
        └── SchemaView.vue             # Schema 递归渲染
```

## 开发调试

```bash
npm install
npm run dev        # http://localhost:3000
npm run format     # Prettier 格式化
npm run lint:style # Stylelint 检查
```

修改 `vite.config.ts` 中的 `BACKEND` 常量可切换后端地址（默认 `http://localhost:8080`）。

## 数据接口说明

| 接口 | 说明 |
|------|------|
| `/springdoc-plus-gateway/openapi/groups` | 返回文档组列表，单服务和网关 starter 均提供该接口 |
| `/springdoc-plus-gateway/ui-config` | 返回排序策略、鉴权默认值等 UI 配置，接口不可用时前端使用本地默认值 |

## 构建 & 部署

```bash
npm run build
# 将 dist/ 复制到 src/main/resources/META-INF/resources/springdoc-plus-ui/
npm run copy
```

Spring Boot controller（WebFlux）：
```java
@GetMapping(value = "/doc.html", produces = MediaType.TEXT_HTML_VALUE)
public Mono<ResponseEntity<Resource>> docHtml() {
    return Mono.fromCallable(() ->
        resourceLoader.getResource("classpath:/META-INF/resources/springdoc-plus-ui/index.html")
    ).map(r -> ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(r));
}
```
