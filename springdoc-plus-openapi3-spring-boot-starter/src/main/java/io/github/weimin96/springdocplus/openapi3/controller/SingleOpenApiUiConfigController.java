package io.github.weimin96.springdocplus.openapi3.controller;

import io.github.weimin96.springdocplus.core.model.SpringdocPlusUiConfig;
import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 单服务 UI 配置控制器。
 *
 * @author pwm
 */
@RestController
public class SingleOpenApiUiConfigController {

    private static final Logger log = LoggerFactory.getLogger(SingleOpenApiUiConfigController.class);

    private final SpringdocPlusOpenApi3Properties props;

    public SingleOpenApiUiConfigController(SpringdocPlusOpenApi3Properties props) {
        this.props = props;
    }

    @GetMapping(value = "/springdoc-plus-gateway/ui-config", produces = MediaType.APPLICATION_JSON_VALUE)
    public SpringdocPlusUiConfig config() {
        SpringdocPlusUiConfig config = new SpringdocPlusUiConfig();
        config.setTagsSorter(props.getTagsSorter());
        config.setOperationsSorter(props.getOperationsSorter());
        if (props.getAuth() != null) {
            config.setAuthEnabled(props.getAuth().isEnabled());
            config.setAuthHeaderName(props.getAuth().getHeaderName());
            config.setAuthDefaultPrefix(props.getAuth().getDefaultPrefix());
            config.setAuthPersist(props.getAuth().isPersist());
        }
        if (props.getBasic() != null) {
            config.setGatewayBasicEnabled(props.getBasic().isEnabled());
        }
        log.debug("返回单服务 UI 配置");
        return config;
    }
}
