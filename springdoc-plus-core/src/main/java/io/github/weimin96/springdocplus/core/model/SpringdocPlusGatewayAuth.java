package io.github.weimin96.springdocplus.core.model;

import lombok.Data;

/**
 * @author pwm
 */
@Data
public class SpringdocPlusGatewayAuth {

    /**
     * 是否启用“统一透传 header”。
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
}
