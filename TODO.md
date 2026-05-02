一、功能层面
- [x] 鉴权方式过于单一
   目前只支持 Bearer Token 透传和 Basic Auth 保护页面，缺少对 OAuth2 / OpenID Connect 的支持。很多实际项目用 Keycloak 或 Spring Authorization Server，Knife4j 和原版 Swagger UI 都有 OAuth2 flow 配置，这块是明显的缺口。
- [x] Discover 模式健康检查缺失
   自动聚合时，若下游服务挂掉或 /v3/api-docs 不可达，前端只会加载失败，没有明确的降级展示（比如标注"服务离线"、灰掉该分组）。用户无法区分"服务不存在"还是"文档接口故障"。
- [x] DOCX 导出功能较弱
   目前导出的是固定 Word 模板，缺乏：

自定义模板上传能力（让用户替换公司统一样式的模板）
导出格式选项（Markdown / HTML / OpenAPI JSON/YAML 原始文件）

- [x] 接口搜索 / 过滤
   接口数量多时，左侧列表缺少全局搜索框和按 HTTP Method 过滤，这是 Knife4j 用户迁移过来首先会感受到的缺口。
- [x] Mock 数据生成
   调试面板根据 Schema 自动生成示例值，但对于 $ref 嵌套较深的复杂 Schema，生成质量不稳定。可以考虑用 openapi-sampler 这类专门库来统一处理。

二、安全层面
- [x] Basic Auth 密码明文写 YAML
   目前 basic.password 是普通字符串，用户极容易直接把明文密码提交到 Git。应支持 {bcrypt} 前缀加密或与 Spring 的 PasswordEncoder 集成，并在文档中明确警告。
- [x] Token 持久化到 localStorage 的风险
   persist: true 会把 Bearer Token 存入浏览器 localStorage，存在 XSS 窃取风险。应在文档中说明风险，或提供 sessionStorage 选项，以及增加 Token 有效期提醒。
- [x] 路径遍历校验
   CHANGELOG 里提到"网关静态资源端点添加文件名验证"，说明之前存在路径遍历问题。建议补充对应的安全测试用例并在 README 的安全章节中做说明，增强使用者信心。

三、工程/构建层面
- [ ] 前端构建耦合 Maven 生命周期
   springdoc-plus-ui 在 Maven build 时触发前端重建，导致每次 mvn package 都要跑 pnpm run deploy，在 CI 环境中如果 Node.js 环境不一致很容易失败。建议将前端产物的重建设计为可选（-Pfrontend profile），默认使用已提交的 dist 产物。
- [ ] JaCoCo 90% 覆盖率门槛与实际情况
    pom.xml 中配置了 LINE 覆盖率 minimum=0.90，但从项目规模和迭代速度来看，这个门槛较高，且 Coveralls badge 显示覆盖率可能并未达到。建议先把门槛调整到实际可持续维护的水平（如 70%），避免 CI 卡住或被跳过（-DskipTests）。
- [ ] springdoc-plus-samples 打包到 release
    父 pom 的 <modules> 包含了 springdoc-plus-samples，意味着 mvn package 时 sample 工程也会被构建并参与检查，而 sample 本身不应发布到 Maven Central。建议把 samples 模块放进独立 profile 或通过 <skip> 配置排除其 deploy。

四、文档和开发体验
- [ ] 英文 README 缺失
    项目定位是支持 Spring Boot 4 生态的 Knife4j 替代品，有一定国际化潜力，但 README 完全是中文。加一份英文版（或双语切换）能大幅提升 Star 数和 Issue 质量。
- [ ] x-order 使用方式繁琐
    当前要通过 @Extension + @ExtensionProperty 两层注解来设置排序，比 Knife4j 的 @ApiSupport(order=1) 繁琐很多。可以考虑提供一个简化注解 @DocOrder(1) 作为语法糖，降低迁移成本。
- [ ] 没有 Issue / PR 模板
    当前 Issues 为 0，但随着用户增多问题会进来。.github/ 目录下可以加 ISSUE_TEMPLATE 和 PULL_REQUEST_TEMPLATE，方便收集必要的环境信息（Spring Boot 版本、starter 版本、错误日志等），减少来回沟通成本。

五、前端层面
- [ ] 前端错误处理
    CHANGELOG 提到"文档组接口加载失败时前端没有明确错误提示"在 0.1.6 才修复，说明错误边界处理整体还不够健壮。建议对所有 API 请求统一加全局错误拦截，区分 network error、401/403、5xx 并给出对用户友好的说明。
