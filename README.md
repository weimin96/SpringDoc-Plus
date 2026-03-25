# SpringDoc-Plus

[![Maven Central](https://img.shields.io/maven-central/v/io.github.weimin96/springdoc-plus.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.weimin96/springdoc-plus)
[![License](https://img.shields.io/github/license/weimin96/springdoc-plus)](https://github.com/weimin96/springdoc-plus/blob/main/LICENSE)
[![Java Version](https://img.shields.io/badge/Java-21-blue)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-green)](https://spring.io/projects/spring-cloud)
[![springdoc](https://img.shields.io/badge/springdoc-3.0.1-green)](https://springdoc.org/)

> 由于 Knife4j 许久不发版，参考 Knife4j 方案，重新实现适配 Spring Boot 4.0 + springdoc 3.0 的 OpenAPI 文档聚合与 UI。

## 特性

- 网关聚合模式：支持手动配置（manual）和服务发现（discover）两种模式
- 双模式自动检测：网关模式 / 子服务模式自动识别
- 自实现 OpenAPI 3.x 解析与渲染，无第三方运行时依赖
- 支持 Basic 认证保护
- 支持分组排序策略
- 自定义 docx 模板导出

## 技术栈

- Java 21
- Spring Boot 4.0.4
- Spring Cloud 2025.1.0
- springdoc-openapi 3.0.1
- Vue 3 + TypeScript + Tailwind CSS v4 + Vite 7

## 模块说明

| 模块 | 说明 |
|------|------|
| springdoc-plus-core | 核心模块，包含共享的枚举和模型类 |
| springdoc-plus-openapi3-spring-boot-starter | 单服务 OpenAPI3 文档的 starter |
| springdoc-plus-gateway-spring-boot-starter | Spring Cloud Gateway 聚合文档的 starter |
| springdoc-plus-web | 前端模块 |
| springdoc-plus-ui | 前端打包模块 |

## 快速开始

### 子服务

`pom` 文件引入依赖

```pom
<dependency>
    <groupId>io.github.weimin96</groupId>
    <artifactId>springdoc-plus-openapi3-spring-boot-starter</artifactId>
    <version>0.1.0<version>
</dependency>
```

### 网关

`pom` 文件引入依赖

```pom
<dependency>
    <groupId>io.github.weimin96</groupId>
    <artifactId>springdoc-plus-gateway-spring-boot-starter</artifactId>
    <version>0.1.0<version>
</dependency>
```

`application.yml` 配置示例
```angular2html
spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              # 关闭服务发现
              enabled: false
          routes:
            - id: system-service
              uri: lb://system-service
              predicates:
                - Path=/system/**
              filters:
                - StripPrefix=1

springdoc-plus:
  gateway:
    enabled: true
    # 手动配置
    strategy: manual
    # 关闭 Basic 保护
    basic:
      enabled: false
      username: admin
      password: admin
    # UI 鉴权透传
    auth:
      enabled: true
      header-name: Authorization
      default-prefix: Bearer
      persist: true

    # manual 模式数据源
    routes:
      - name: 系统服务
        service-name: system-service
        url: /system/v3/api-docs
        context-path: /system
        order: 1

    # discover 模式数据源
    discover:
      enabled: true
      version: openapi3
      excluded-services:
        # 排除网关服务
        - ${spring.application.name}
```

访问{网关}{端口}/doc.html

## demo示例

### 1) 构建

```bash
mvn -q -DskipTests package
```

### 2) 运行示例

```bash
# 终端1：启动用户服务
cd springdoc-plus-examples/user-service-example
mvn -q spring-boot:run

# 终端2：启动订单服务
cd springdoc-plus-examples/order-service-example
mvn -q spring-boot:run

# 终端3：启动网关服务
cd springdoc-plus-examples/gateway-example
mvn -q spring-boot:run
```

### 3) 访问

- 网关聚合 UI：`http://localhost:8080/doc.html`
- 用户服务 UI：`http://localhost:8081/doc.html`

## 网关聚合配置

### Manual 模式

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
      - name: 订单服务
        service-name: order-service
        url: /order-service/v3/api-docs
        context-path: /order-service
        order: 2
```

### Discover 模式

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

**discover 模式依赖条件：**

1. Gateway 开启 `spring.cloud.gateway.discovery.locator.enabled=true`
2. 服务注册发现（如 Spring Cloud Simple Discovery）
3. 可选：通过 `resolveContextPathFromGatewayRoutes` 从 Gateway 路由推断 contextPath

## 配置参数说明

### 网关聚合配置（springdoc-plus.gateway）

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | Boolean | `false` | 是否启用网关聚合功能 |
| `strategy` | GatewayStrategy | `MANUAL` | 聚合策略：MANUAL（手动）或 DISCOVER（服务发现） |
| `routes` | List<GatewayRoute> | - | 手动模式下的路由配置列表 |
| `discover.enabled` | Boolean | `false` | 是否启用服务发现 |
| `discover.version` | OpenApiVersion | `openapi3` | 服务发现的 API 版本 |
| `discover.excluded-services` | List<String> | - | 排除的服务列表 |
| `discover.resolve-context-path-from-gateway-routes` | Boolean | `true` | 是否从 Gateway 路由推断 contextPath |
| `order-strategy` | GroupOrderStrategy | `order` | 分组排序策略：alpha 或 order |
| `auth.enabled` | Boolean | `false` | 是否启用 UI 鉴权 |
| `auth.username` | String | - | UI 鉴权用户名 |
| `auth.password` | String | - | UI 鉴权密码（BCrypt 加密） |
| `basic.enabled` | Boolean | `false` | 是否启用 Basic 认证保护 |
| `basic.username` | String | - | Basic 认证用户名 |
| `basic.password` | String | - | Basic 认证密码 |

### GatewayRoute 字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 是 | 分组显示名称 |
| `service-name` | String | 是 | 服务名称 |
| `url` | String | 是 | OpenAPI 文档地址 |
| `context-path` | String | 否 | 服务上下文路径 |
| `order` | Integer | 否 | 排序权重，数值越小越靠前 |
| `group` | String | 否 | 分组标识 |

### 单服务配置（springdoc-plus.openapi3）

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | Boolean | `false` | 是否启用单服务 OpenAPI 文档 |
| `api-docs.path` | String | `/v3/api-docs` | OpenAPI 文档路径 |

## 许可证

[LICENSE](LICENSE) 

## 贡献指南

欢迎提交 Issue 和 Pull Request。