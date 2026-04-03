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
}
