package io.github.weimin96.springdocplus.core.model;

/**
 * 网关文档 UI 认证配置模型。
 * <p>
 * 配置 UI 侧的认证信息透传，用于在”Try it out”请求中携带认证 Header。
 * 该功能主要由前端在 requestInterceptor 中实现，后端不做强制校验。
 *
 * @author pwm
 */
public class SpringdocPlusGatewayAuth {

    /**
     * 是否启用”统一透传 header”。
     * <p>
     * 说明：该功能主要给 UI 使用，
     * 由前端在 requestInterceptor 中加 header。后端不做强制校验。
     */
    private boolean enabled = true;

    /**
     * 默认 Header 名称：Authorization
     */
    private String headerName = "Authorization";

    /**
     * 默认前缀（可空），例如 Bearer
     */
    private String defaultPrefix = "";

    /**
     * 是否在本地存储记住
     */
    private boolean persist = true;

    /**
     * 无参构造器。
     */
    public SpringdocPlusGatewayAuth() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }

    public boolean isPersist() {
        return persist;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }
}
