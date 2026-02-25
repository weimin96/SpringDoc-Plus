package io.github.weimin96.springdocplus.core.model;

import lombok.Data;

import java.util.List;

/**
 * @author pwm
 */
@Data
public class GatewayRoute {
    /**
     * UI显示名称
     */
    private String name;

    /**
     * 服务名（discover 时用于匹配配置；manual 时可选）
     */
    private String serviceName;

    /**
     * OpenAPI 文档地址（通常走网关转发路径）
     */
    private String url;

    /**
     * 用于在 UI 中拼装“Try it out”请求的 basePath（可选）
     */
    private String contextPath;

    /**
     * springdoc group（可选）
     */
    private String group;

    /**
     * 多分组（discover 模式下可配置）
     */
    private List<String> groupNames;

    /**
     * 排序
     */
    private Integer order = 0;
}
