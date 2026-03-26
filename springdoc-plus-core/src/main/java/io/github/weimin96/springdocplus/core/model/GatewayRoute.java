package io.github.weimin96.springdocplus.core.model;

import java.util.List;

/**
 * 网关路由配置模型。
 * <p>
 * 用于配置网关聚合文档模式下各子服务的路由信息，
 * 包含服务名、OpenAPI 文档地址、上下文路径、分组及排序等配置。
 *
 * @author pwm
 */
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
     * 用于在 UI 中拼装”Try it out”请求的 basePath（可选）
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

    /**
     * 无参构造器。
     * <p>
     * Javadoc 要求必须存在默认构造器的注释说明。
     */
    public GatewayRoute() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
