# SpringDoc-Plus

SpringDoc-Plus is a Knife4j-style OpenAPI documentation UI for Spring Boot 4 and springdoc-openapi 3. It supports standalone services and Spring Cloud Gateway aggregation.

## Features

- Gateway aggregation with manual routes or service discovery.
- Standalone OpenAPI UI for regular Spring MVC services.
- Built-in API request panel with path, query, header, cookie, JSON, form and multipart support.
- Optional Basic Auth protection for documentation pages.
- Bearer Token, custom header and OAuth2 token helper support for API debugging.
- Export to DOCX, Markdown, HTML, OpenAPI JSON and OpenAPI YAML.
- `x-order` sorting and the `@DocOrder` shortcut annotation.

## Requirements

- Java 21
- Spring Boot 4.0.x
- Spring Cloud 2025.1.x
- springdoc-openapi 3.0.x

## Standalone Usage

```xml
<dependency>
    <groupId>io.github.weimin96</groupId>
    <artifactId>springdoc-plus-openapi3-spring-boot-starter</artifactId>
    <version>0.1.7</version>
</dependency>
```

Visit:

```text
http://localhost:8080/doc.html
```

## Gateway Usage

```xml
<dependency>
    <groupId>io.github.weimin96</groupId>
    <artifactId>springdoc-plus-gateway-spring-boot-starter</artifactId>
    <version>0.1.7</version>
</dependency>
```

Manual aggregation example:

```yaml
springdoc-plus:
  gateway:
    enabled: true
    strategy: manual
    routes:
      - name: User Service
        service-name: user-service
        url: /user-service/v3/api-docs
        context-path: /user-service
        order: 1
```

Visit:

```text
http://localhost:8080/doc.html
```

## Sorting With `@DocOrder`

```java
@DocOrder(1)
@GetMapping("/users")
public List<User> users() {
    return List.of();
}
```

The annotation is converted to the OpenAPI `x-order` extension, so the UI can sort operations when `operations-sorter: order` is enabled.

## Security Notes

For Basic Auth, production deployments should prefer `{bcrypt}` passwords and HTTPS:

```yaml
springdoc-plus:
  openapi3:
    basic:
      enabled: true
      username: admin
      password: "{bcrypt}$2a$10$..."
```

Token storage in the UI supports `sessionStorage` and `localStorage`. Prefer `sessionStorage` unless long-lived local persistence is explicitly required.

## Frontend Development

```bash
cd springdoc-plus-web
pnpm install
pnpm dev
pnpm run build
```

Maven rebuilds the frontend only when the `frontend` profile is enabled:

```bash
mvn -q -Pfrontend -pl springdoc-plus-ui package
```

Samples are included only when the `samples` profile is enabled:

```bash
mvn -q -Psamples package
```
