# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

SpringDoc-Plus 是一个参考 Knife4j 4.5 的网关聚合文档方案，适配 Spring Boot 4.0 + springdoc 3.0。项目采用多模块 Maven 结构。

### 技术栈

- Java 21
- Spring Boot 4.0.1
- Spring Cloud 2025.1.0
- Springdoc OpenAPI 3.0.1

## 常用命令

### 构建

```bash
mvn -q -DskipTests package
```

### 运行示例

```bash
# 用户服务
cd springdoc-plus-examples/user-service-example
mvn -q spring-boot:run

# 订单服务
cd springdoc-plus-examples/order-service-example
mvn -q spring-boot:run

# 网关服务
cd springdoc-plus-examples/gateway-example
mvn -q spring-boot:run
```

### 访问地址

- 网关聚合 UI：`http://localhost:8080/doc.html`
- 网关聚合分组列表：`http://localhost:8080/springdoc-plus-gateway/openapi/groups`

## 模块架构

### springdoc-plus-core
核心模块，包含共享的枚举和模型类：
- `GatewayStrategy`: 网关聚合策略（MANUAL/DISCOVER）
- `GroupOrderStrategy`: 分组排序策略（alpha/order）
- `OpenApiVersion`: OpenAPI 版本
- `GatewayRoute`: 网关路由模型，包含 name、serviceName、url、contextPath、group、order 等字段
- `SpringdocPlusGatewayAuth`: UI 侧鉴权配置
- `SpringdocPlusGatewayHttpBasic`: Basic 保护配置

### springdoc-plus-ui
前端静态资源，以 webjar 方式被 starter 引入。

### springdoc-plus-openapi3-spring-boot-starter
单服务 OpenAPI3 文档的 starter：
- 自动配置条件：类 `org.springdoc.webmvc.ui.SwaggerConfig` 存在
- 配置前缀：`springdoc-plus.openapi3`
- 提供 `DocHtmlController`：返回 doc.html 页面
- 提供 `SingleOpenApiGroupsController`：返回单服务分组列表

### springdoc-plus-gateway-spring-boot-starter
Spring Cloud Gateway 聚合文档的 starter：
- 自动配置条件：类 `org.springframework.cloud.gateway.route.RouteDefinition` 存在
- 配置前缀：`springdoc-plus.gateway`
- 核心类：
  - `SpringdocPlusGatewayProperties`: 配置属性，包含 strategy、routes、discover、auth、basic 等
  - `DiscoverGroupsService`: 生成分组列表，支持 manual 和 discover 两种模式
  - `SpringdocPlusGatewayOpenApiController`: 提供 `/springdoc-plus-gateway/openapi/groups` 接口
  - `GatewayRouteDefinitionResolver`: 从 Gateway 路由定义推断 contextPath
  - `BasicAuthWebFilter`: Basic 认证过滤器

## 网关聚合配置

### manual 模式

手动配置每个服务的路由信息：

```yaml
springdoc-plus:
  gateway:
    enabled: true
    strategy: manual
    routes:
      - name: 用户服务
        service-name: user-service
        url: /user-service/v3/api-docs
        context-path: /user-service
        order: 1
```

### discover 模式

基于服务发现自动发现服务：

```yaml
springdoc-plus:
  gateway:
    enabled: true
    strategy: discover
    discover:
      enabled: true
      version: openapi3
      excluded-services:
        - gateway-example
```

discover 模式需要：
1. Gateway 开启 `spring.cloud.gateway.discovery.locator.enabled=true`
2. 服务注册发现（如 Spring Cloud Simple Discovery）
3. 可选：通过 `resolveContextPathFromGatewayRoutes` 从 Gateway 路由推断 contextPath

## 设计要点

1. `DiscoverGroupsService` 是核心服务，负责生成可用的分组列表：
   - manual 模式直接使用 `routes` 配置
   - discover 模式结合 DiscoveryClient 和 Gateway 路由定义生成分组
   - 支持通过 `serviceConfig` 个性化配置每个服务
   - 支持排除特定服务

2. 分组 URL 构造：
   - 默认使用 `contextPath + openapi3Url`
   - 多分组时附加 `?group=xxx` 参数

3. 排序策略：
   - 通过 `order` 字段控制分组顺序
   - 支持标签和操作的排序策略
