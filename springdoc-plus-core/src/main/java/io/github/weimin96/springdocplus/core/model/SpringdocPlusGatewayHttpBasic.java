package io.github.weimin96.springdocplus.core.model;

import lombok.Data;

/**
 * @author pwm
 */
@Data
public class SpringdocPlusGatewayHttpBasic {
    /**
     * 是否启用 Basic
     */
    private boolean enabled = false;
    private String username;
    private String password;
}
