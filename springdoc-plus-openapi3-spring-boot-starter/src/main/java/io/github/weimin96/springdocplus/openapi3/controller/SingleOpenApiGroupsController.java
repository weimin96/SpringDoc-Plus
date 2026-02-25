package io.github.weimin96.springdocplus.openapi3.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 复用网关 UI 的同一套前端：它只依赖 /springdoc-plus-gateway/openapi/groups。
 * 在单体场景下也提供同名接口，返回单个分组即可。
 *
 * @author pwm
 */
@RestController
public class SingleOpenApiGroupsController {

    @GetMapping(value = "/springdoc-plus-gateway/openapi/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> groups() {
        return Map.of(
                "groups",
                List.of(
                        Map.of(
                                "name", "default",
                                "url", "/v3/api-docs",
                                "contextPath", "/",
                                "order", 0
                        )
                )
        );
    }
}
