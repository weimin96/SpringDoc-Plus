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

    /**
     * 获取 UI 显示名称
     *
     * @return UI 显示名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置 UI 显示名称
     *
     * @param name UI 显示名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取服务名
     *
     * @return 服务名
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 设置服务名
     *
     * @param serviceName 服务名
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * 获取 OpenAPI 文档地址
     *
     * @return OpenAPI 文档地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置 OpenAPI 文档地址
     *
     * @param url OpenAPI 文档地址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取上下文路径
     *
     * @return 上下文路径
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * 设置上下文路径
     *
     * @param contextPath 上下文路径
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * 获取 springdoc group
     *
     * @return springdoc group
     */
    public String getGroup() {
        return group;
    }

    /**
     * 设置 springdoc group
     *
     * @param group springdoc group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 获取多分组列表
     *
     * @return 多分组列表
     */
    public List<String> getGroupNames() {
        return groupNames;
    }

    /**
     * 设置多分组列表
     *
     * @param groupNames 多分组列表
     */
    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
    }

    /**
     * 获取排序值
     *
     * @return 排序值
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * 设置排序值
     *
     * @param order 排序值
     */
    public void setOrder(Integer order) {
        this.order = order;
    }
}
