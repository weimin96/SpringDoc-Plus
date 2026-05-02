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
     * OAuth2 / OpenID Connect Token 获取配置。
     */
    private OAuth2 oauth2 = new OAuth2();

    /**
     * 无参构造器。
     */
    public SpringdocPlusGatewayAuth() {
    }

    /**
     * 获取是否启用统一透传 header
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用统一透传 header
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取 Header 名称
     *
     * @return Header 名称
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * 设置 Header 名称
     *
     * @param headerName Header 名称
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * 获取默认前缀
     *
     * @return 默认前缀
     */
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    /**
     * 设置默认前缀
     *
     * @param defaultPrefix 默认前缀
     */
    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }

    /**
     * 获取是否在本地存储记住
     *
     * @return 是否在本地存储记住
     */
    public boolean isPersist() {
        return persist;
    }

    /**
     * 设置是否在本地存储记住
     *
     * @param persist 是否在本地存储记住
     */
    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    /**
     * 获取 OAuth2 / OpenID Connect Token 获取配置。
     *
     * @return OAuth2 / OpenID Connect Token 获取配置
     */
    public OAuth2 getOauth2() {
        return oauth2;
    }

    /**
     * 设置 OAuth2 / OpenID Connect Token 获取配置。
     *
     * @param oauth2 OAuth2 / OpenID Connect Token 获取配置
     */
    public void setOauth2(OAuth2 oauth2) {
        this.oauth2 = oauth2;
    }

    /**
     * OAuth2 / OpenID Connect Token 获取配置。
     */
    public static class OAuth2 {

        /**
         * 是否在 UI 中启用 OAuth2 Token 获取面板。
         */
        private boolean enabled = false;

        /**
         * Token 端点地址。
         */
        private String tokenUrl = "";

        /**
         * 客户端 ID。
         */
        private String clientId = "";

        /**
         * 默认 scope，多个 scope 使用空格分隔。
         */
        private String scope = "";

        /**
         * 授权模式，默认使用 client_credentials。
         */
        private String grantType = "client_credentials";

        /**
         * 无参构造器。
         */
        public OAuth2() {
        }

        /**
         * 获取是否启用 OAuth2 Token 获取面板。
         *
         * @return 是否启用
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置是否启用 OAuth2 Token 获取面板。
         *
         * @param enabled 是否启用
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * 获取 Token 端点地址。
         *
         * @return Token 端点地址
         */
        public String getTokenUrl() {
            return tokenUrl;
        }

        /**
         * 设置 Token 端点地址。
         *
         * @param tokenUrl Token 端点地址
         */
        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        /**
         * 获取客户端 ID。
         *
         * @return 客户端 ID
         */
        public String getClientId() {
            return clientId;
        }

        /**
         * 设置客户端 ID。
         *
         * @param clientId 客户端 ID
         */
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        /**
         * 获取默认 scope。
         *
         * @return 默认 scope
         */
        public String getScope() {
            return scope;
        }

        /**
         * 设置默认 scope。
         *
         * @param scope 默认 scope
         */
        public void setScope(String scope) {
            this.scope = scope;
        }

        /**
         * 获取授权模式。
         *
         * @return 授权模式
         */
        public String getGrantType() {
            return grantType;
        }

        /**
         * 设置授权模式。
         *
         * @param grantType 授权模式
         */
        public void setGrantType(String grantType) {
            this.grantType = grantType;
        }
    }
}
