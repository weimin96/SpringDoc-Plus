# springdoc-plus（MVP）

> 目标：参考 Knife4j 4.5 的“网关聚合 + 统一入口 doc.html + 手动/服务发现”思路，重新实现一套 **适配 Spring Boot 4.0 + springdoc 3.0** 的聚合与 UI（重构）方案。

## 模块

- `springdoc-plus-core`：核心模型与通用工具
- `springdoc-plus-ui`：重构后的前端静态资源（以 webjar 的方式被 starter 引入）
- `springdoc-plus-gateway-spring-boot-starter`：Spring Cloud Gateway 聚合 starter（manual + discover），并提供 `doc.html`
- `springdoc-plus-examples`
  - `gateway-example`：演示网关聚合
  - `user-service-example`：演示下游服务（springdoc3）
  - `order-service-example`：演示下游服务（springdoc3）

## 快速开始

### 1) 构建

```bash
mvn -q -DskipTests package
```

### 2) 运行示例

```bash
# 终端1
cd examples/user-service-example
mvn -q spring-boot:run

# 终端2
cd examples/order-service-example
mvn -q spring-boot:run

# 终端3
cd examples/gateway-example
mvn -q spring-boot:run
```

### 3) 访问

- 网关聚合 UI：`http://localhost:8080/doc.html`
- 网关聚合分组列表（供 UI 调用）：`http://localhost:8080/springdoc-plus-gateway/openapi/groups`

## 网关聚合配置

### manual 模式

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

### discover 模式（基于 DiscoveryClient）

> 需要：
> 1. gateway 开启 `spring.cloud.gateway.discovery.locator.enabled=true`
> 2. 服务注册发现（示例使用 Spring Cloud Simple Discovery）

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

## 说明

- 这是一个 **可运行的最小可用版本（MVP）**，重点把“Boot4 + springdoc3 + Gateway 聚合 + 重构 UI”主链路跑通。
- 目前仅实现 OpenAPI3（/v3/api-docs）的聚合；Swagger2 的兼容可以作为后续迭代。

