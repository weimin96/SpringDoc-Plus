package io.github.weimin96.springdocplus.gateway.controller.dto;

import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import lombok.Data;

/**
 * UI 配置响应模型。
 * <p>
 * 提供给前端的配置信息，包括排序策略、认证设置等。
 *
 * @author pwm
 */
@Data
public class SpringdocPlusUiConfig {
    /**
     * Tag 排序策略
     */
    private GroupOrderStrategy tagsSorter = GroupOrderStrategy.alpha;

    /**
     * Operation 排序策略
     */
    private GroupOrderStrategy operationsSorter = GroupOrderStrategy.alpha;

    /**
     * 是否展示 Try it out
     */
    private boolean tryItOutEnabled = true;

    /**
     * UI 鉴权开关
     */
    private boolean authEnabled = true;

    /**
     * 认证 Header 名称
     */
    private String authHeaderName = "Authorization";

    /**
     * 认证默认前缀
     */
    private String authDefaultPrefix = "";

    /**
     * 认证信息是否持久化
     */
    private boolean authPersist = true;

    /**
     * 网关 basic 防护提示（UI 用于提示用户可能需要 Basic）
     */
    private boolean gatewayBasicEnabled = false;

    /**
     * 无参构造器
     */
    public SpringdocPlusUiConfig() {
    }
}
