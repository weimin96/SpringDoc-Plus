package io.github.weimin96.springdocplus.gateway.controller.dto;

import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import lombok.Data;

/**
 * @author pwm
 */
@Data
public class SpringdocPlusUiConfig {
    private GroupOrderStrategy tagsSorter = GroupOrderStrategy.alpha;
    private GroupOrderStrategy operationsSorter = GroupOrderStrategy.alpha;

    /**
     * 是否展示 Try it out
     */
    private boolean tryItOutEnabled = true;

    /**
     * UI 鉴权开关
     */
    private boolean authEnabled = true;
    private String authHeaderName = "Authorization";
    private String authDefaultPrefix = "";
    private boolean authPersist = true;

    /**
     * 网关 basic 防护提示（UI 用于提示用户可能需要 Basic）
     */
    private boolean gatewayBasicEnabled = false;
}
