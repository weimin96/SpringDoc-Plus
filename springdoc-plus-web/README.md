# SpringDoc Plus UI

一个基于 Vue 3 + TypeScript + Tailwind CSS v4 + Vite 7 构建的 Swagger UI 重构页面，参考 Knife4j 风格，扁平化现代设计。

## 项目结构

```
springdoc-ui/
├── src/
│   ├── App.vue                     # 主应用 Shell（侧边栏+顶栏+内容区）
│   ├── main.ts                     # 入口文件
│   ├── style.css                   # 全局样式（Tailwind v4 + Swagger UI 覆写）
│   ├── types/index.ts              # TypeScript 类型定义
│   ├── composables/
│   │   ├── useConfig.ts            # 本地配置管理（localStorage）
│   │   └── useSwagger.ts           # Swagger UI 初始化与参数构建
│   └── components/
│       └── SettingsModal.vue       # 配置弹窗组件
├── index.html                      # Vite 入口 HTML
├── index-standalone.html           # ★ 独立 HTML（直接用于 Spring Boot）
├── vite.config.ts
├── tsconfig.json / tsconfig.app.json / tsconfig.node.json
└── package.json
```

## 快速使用（Spring Boot 独立文件）

### 方式一：直接使用 `index-standalone.html`

将 `index-standalone.html` 复制到 Spring Boot 项目的 `src/main/resources/META-INF/resources/springdoc-plus-ui/` 目录并重命名为 `index.html`。

确保 `swagger-ui-dist` 以 WebJar 方式引入：

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>swagger-ui</artifactId>
    <version>5.x.x</version>
</dependency>
```

Spring Boot 控制器示例：

```java
@GetMapping(value = "/doc.html", produces = MediaType.TEXT_HTML_VALUE)
public Mono<ResponseEntity<Resource>> docHtml() {
    return Mono.fromCallable(() ->
        resourceLoader.getResource("classpath:/META-INF/resources/springdoc-plus-ui/index.html")
    ).map(resource -> ResponseEntity.ok()
        .contentType(MediaType.TEXT_HTML)
        .body(resource));
}
```

### 方式二：Vue 3 源码构建

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本（输出到 dist/）
npm run build
```

构建后将 `dist/` 目录内容复制到 `src/main/resources/META-INF/resources/springdoc-plus-ui/`。

## 功能特性

- **扁平化现代 UI** —— 干净的侧边栏 + 顶栏布局，无冗余装饰
- **文档组切换** —— 侧边栏列出所有 API 文档组，支持关键词搜索
- **Swagger UI 美化** —— HTTP 方法色块、圆角卡片、mono 字体路径展示
- **排序配置** —— Tags / Operations 支持 alpha 字母序或 x-order 自定义序
- **请求鉴权** —— Header 注入（Authorization / X-Token 等），支持 Bearer/Basic 前缀
- **持久化选项** —— Token 可选 localStorage 持久化
- **侧边栏折叠** —— 点击汉堡按钮收起/展开，获得更多空间
- **响应式** —— 适配桌面宽度

## API 接口约定

页面依赖以下两个后端接口：

| 接口 | 返回格式 | 说明 |
|------|----------|------|
| `GET /springdoc-plus-gateway/openapi/groups` | `{ groups: [{name, url}] }` | 所有文档组列表 |
| `GET /springdoc-plus-gateway/ui-config` | `ServerUiConfig` | 服务端 UI 配置 |

`ServerUiConfig` 结构：
```json
{
  "tagsSorter": "alpha",
  "operationsSorter": "alpha",
  "gatewayBasicEnabled": false,
  "authEnabled": false,
  "authHeaderName": "Authorization",
  "authDefaultPrefix": "Bearer",
  "authPersist": false
}
```
