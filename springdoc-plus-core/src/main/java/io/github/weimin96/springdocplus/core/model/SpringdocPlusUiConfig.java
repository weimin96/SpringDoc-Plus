package io.github.weimin96.springdocplus.core.model;

import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;

/**
 * UI 配置响应模型。
 *
 * @author pwm
 */
public class SpringdocPlusUiConfig {

    private GroupOrderStrategy tagsSorter = GroupOrderStrategy.alpha;

    private GroupOrderStrategy operationsSorter = GroupOrderStrategy.alpha;

    private boolean tryItOutEnabled = true;

    private boolean authEnabled = true;

    private String authHeaderName = "Authorization";

    private String authDefaultPrefix = "";

    private boolean authPersist = true;

    private boolean gatewayBasicEnabled = false;

    private boolean oauth2Enabled = false;

    private String oauth2TokenUrl = "";

    private String oauth2ClientId = "";

    private String oauth2Scope = "";

    private String oauth2GrantType = "client_credentials";

    public GroupOrderStrategy getTagsSorter() {
        return tagsSorter;
    }

    public void setTagsSorter(GroupOrderStrategy tagsSorter) {
        this.tagsSorter = tagsSorter;
    }

    public GroupOrderStrategy getOperationsSorter() {
        return operationsSorter;
    }

    public void setOperationsSorter(GroupOrderStrategy operationsSorter) {
        this.operationsSorter = operationsSorter;
    }

    public boolean isTryItOutEnabled() {
        return tryItOutEnabled;
    }

    public void setTryItOutEnabled(boolean tryItOutEnabled) {
        this.tryItOutEnabled = tryItOutEnabled;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    public String getAuthHeaderName() {
        return authHeaderName;
    }

    public void setAuthHeaderName(String authHeaderName) {
        this.authHeaderName = authHeaderName;
    }

    public String getAuthDefaultPrefix() {
        return authDefaultPrefix;
    }

    public void setAuthDefaultPrefix(String authDefaultPrefix) {
        this.authDefaultPrefix = authDefaultPrefix;
    }

    public boolean isAuthPersist() {
        return authPersist;
    }

    public void setAuthPersist(boolean authPersist) {
        this.authPersist = authPersist;
    }

    public boolean isGatewayBasicEnabled() {
        return gatewayBasicEnabled;
    }

    public void setGatewayBasicEnabled(boolean gatewayBasicEnabled) {
        this.gatewayBasicEnabled = gatewayBasicEnabled;
    }

    public boolean isOauth2Enabled() {
        return oauth2Enabled;
    }

    public void setOauth2Enabled(boolean oauth2Enabled) {
        this.oauth2Enabled = oauth2Enabled;
    }

    public String getOauth2TokenUrl() {
        return oauth2TokenUrl;
    }

    public void setOauth2TokenUrl(String oauth2TokenUrl) {
        this.oauth2TokenUrl = oauth2TokenUrl;
    }

    public String getOauth2ClientId() {
        return oauth2ClientId;
    }

    public void setOauth2ClientId(String oauth2ClientId) {
        this.oauth2ClientId = oauth2ClientId;
    }

    public String getOauth2Scope() {
        return oauth2Scope;
    }

    public void setOauth2Scope(String oauth2Scope) {
        this.oauth2Scope = oauth2Scope;
    }

    public String getOauth2GrantType() {
        return oauth2GrantType;
    }

    public void setOauth2GrantType(String oauth2GrantType) {
        this.oauth2GrantType = oauth2GrantType;
    }
}
