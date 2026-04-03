package io.github.weimin96.springdocplus.openapi3.controller;

import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单服务 OpenAPI 分组控制器。
 * <p>
 * 复用网关 UI 的同一套前端：它只依赖 /springdoc-plus-gateway/openapi/groups。
 * 在单体场景下也提供同名接口，支持配置自定义分组。
 *
 * @author pwm
 */
@RestController
public class SingleOpenApiGroupsController {

    private final SpringdocPlusOpenApi3Properties props;

    /**
     * 构造器
     *
     * @param props 单服务 OpenAPI 配置属性
     */
    public SingleOpenApiGroupsController(SpringdocPlusOpenApi3Properties props) {
        this.props = props;
    }

    /**
     * 获取分组列表
     *
     * @return 单服务分组信息
     */
    @Hidden
    @GetMapping(value = "/springdoc-plus-gateway/openapi/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> groups() {
        List<Map<String, Object>> groupList = new ArrayList<>();

        // 如果用户配置了自定义分组，使用配置值
        if (props.getGroups() != null && !props.getGroups().isEmpty()) {
            for (SpringdocPlusOpenApi3Properties.Group group : props.getGroups()) {
                Map<String, Object> g = new HashMap<>();
                g.put("name", group.getName());
                g.put("url", group.getUrl());
                g.put("contextPath", group.getContextPath());
                g.put("order", group.getOrder());
                groupList.add(g);
            }
        } else {
            // 默认分组
            groupList.add(Map.of(
                    "name", "default",
                    "url", "/v3/api-docs",
                    "contextPath", "/",
                    "order", 0
            ));
        }

        return Map.of("groups", groupList);
    }
}
