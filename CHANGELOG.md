# 更新日志

本项目所有重要的变更都将记录在此文件中。

## [未发布]

## [0.1.8] - 2026-05-17

### 新增

- 新增发布前版本一致性检查脚本，校验根版本、README 示例版本、CHANGELOG 版本记录和子模块硬编码版本。
- 新增前端 Vitest 测试，覆盖 OpenAPI 文档加载竞态、JSON Pointer 引用解析、外部引用降级和 OpenAPI 3.1 类型兼容。

### 优化

- 为 OpenAPI 文档加载增加请求取消和序列保护，避免快速切换分组时旧响应覆盖新状态。
- 增强 Schema `$ref` 解析，支持完整 JSON Pointer、`~0`/`~1` 转义、`components` 下非 `schemas` 节点解析和外部引用降级提示。
- 兼容 OpenAPI 3.1 `type` 数组和 nullable enum，统一 Schema 展示、示例生成、DOCX 导出和模拟请求中的类型判断。
- 优化模拟请求面板的请求体字段识别，提升数组、文件字段、枚举字段和 `*/*` 请求体类型的处理一致性。
- 提升 release workflow 质量闸，发布前执行版本一致性检查、前端测试和 Maven 验证，不再跳过测试。
- release workflow 中的版本提取、Maven settings 生成和发布说明提取改为 PowerShell，减少 Linux-only 脚本依赖。
- CI 前端依赖安装改为 frozen lockfile，并在构建前增加前端测试。
- 同步 `springdoc-plus-ui` 静态资源，确保概览面板最新改动进入 starter 打包产物。

### 修复

- 修复快速切换文档分组时，较慢的旧 OpenAPI 响应可能覆盖当前分组状态的问题。
- 修复 OpenAPI 3.1 `type: ["string", "null"]` 等类型数组在 Schema 展示、参数类型和示例生成中显示不准确的问题。
- 修复仅按 `$ref` 最后一段名称解析引用时，无法处理转义字符、非 schemas 引用和部分合法 JSON Pointer 的问题。
- 修复 `springdoc-plus-ui` 打包产物可能滞后于 `springdoc-plus-web` 源码的问题，旧的 `Shareable` 标记不再保留在 UI 资源中。

### 验证

- 已执行版本一致性检查、前端单元测试、前端生产构建和 Maven `verify`。
- 已确认同步后的 UI 打包产物中不再包含旧的 `Shareable` 标记。

## [0.1.7] - 2026-05-03

### 新增

- 新增 OAuth2 Token 获取面板。
- 新增 HTTP Method 过滤。
- 导出从单一 DOCX 扩展为 DOCX、Markdown、HTML、OpenAPI JSON、OpenAPI YAML。
- Basic Auth 支持 {bcrypt} 前缀密码。
- 前端配置存储 Token 可选择 sessionStorage 或 localStorage。

### 优化

- springdoc 依赖升级。
- JDK 降级为17。
- 文档组离线标记和提示。
- 增强 $ref、组合 Schema、示例值、格式字段和深层嵌套的 Mock 示例生成。
- 静态资源端点路径校验增强。

### 修复

- 隐藏 `/doc.html` 接口在文档里显示。

## [0.1.6] - 2026-04-23

### 修复

- 修复单服务模式下 Basic Auth 未保护 `/springdoc-plus-gateway/**` 接口问题。
- 修复网关模式下 DOCX 默认模板资源无法通过 `/springdoc-plus-ui/docs/模板.docx` 访问的问题。
- 修复前端保存或清空设置后可能丢失服务端 UI 默认配置的问题。
- 修复文档组接口加载失败时前端没有明确错误提示的问题。

### 验证

- 补充单服务 Basic Auth 对 `/springdoc-plus-gateway/**` 的鉴权测试。
- 补充网关 DOCX 模板资源访问和非法路径拒绝测试。

## [0.1.5] - 2026-04-15

### 优化

- 优化复杂结构响应体的展示

## [0.1.4] - 2026-04-06

### 新增

- 单服务增加basic认证

### 变更

- 接口详情页ui优化
- 前端数据缓存增加过期时间

## [0.1.3] - 2026-04-05

### 新增

- 在 Vue UI 中添加了 YAML OpenAPI 解析支持，使用 `js-yaml`。
- 补充大量单元测试。
- 添加了单服务 UI 配置端点，暴露与网关模式相同的 `/springdoc-plus-gateway/ui-config` 契约。
- 在 `springdoc-plus-core` 中添加了共享的 `SpringdocPlusUiConfig` 模型。
- 补充网关异常处理日志。

### 变更

- basic 密码校验处理漏洞修复。
- 网关静态资源端点添加文件名验证。
- 改进网关分组聚合，添加缓存。
- 扩展了单服务 starter 属性，支持分组列表、UI 排序、认证和基本提示设置。
- 将 `springdoc-plus-web` 构建集成到 `springdoc-plus-ui` 的 Maven 生命周期中。
- 前端 ui 样式优化
    - 导出 docx 功能增加使用说明
    - 概览页丰富展示内容
    - 接口详情页路由同步地址栏，方便快速跳转
    - 接口详情页样式优化，模拟请求请求头添加示例
    - 刷新或切换api缓存历史记录
    - 返回html格式增加预览

### 修复

- 修复了 Spring Boot 4 兼容性相关的问题，涉及发布相关的网关错误处理导入。
- 修复了发布打包流程，使前端资产可以在 Maven 构建期间重新构建和复制。
- 修复了概览页接口重复请求
- 修复了切换接口对应的请求体仍然是第一个点击的接口的请求体bug ([#1](https://github.com/weimin96/SpringDoc-Plus/issues/1))
- 修复了 `text/plain` 请求体类型设置失败bug。

## [0.1.2] - 2026-03-26

### 新增

- 在自定义 UI 中添加了 DOCX 导出功能。
- 添加了 Maven Central 发布配置、支持签名、源码和 Javadoc 打包。

### 变更

- 更新了项目发布脚本和工作流相关的发布设置。
- 优化了 README 和内联代码注释中的打包和发布使用说明。
