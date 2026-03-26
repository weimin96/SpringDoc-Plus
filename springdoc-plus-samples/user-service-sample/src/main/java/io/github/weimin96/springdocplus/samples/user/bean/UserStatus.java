package io.github.weimin96.springdocplus.samples.user.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户状态枚举
 *
 * @author pwm
 * @since 2026/3/2 16:18
 */
@Schema(description = "用户状态")
public enum UserStatus {
    /** 激活 */
    @Schema(description = "激活")
    ACTIVE,
    /** 未激活 */
    @Schema(description = "未激活")
    INACTIVE,
    /** 封禁 */
    @Schema(description = "封禁")
    BANNED,
    /** 待审核 */
    @Schema(description = "待审核")
    PENDING
}
