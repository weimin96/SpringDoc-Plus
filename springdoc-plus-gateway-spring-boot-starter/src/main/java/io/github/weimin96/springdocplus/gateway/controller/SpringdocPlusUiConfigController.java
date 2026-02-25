package io.github.weimin96.springdocplus.gateway.controller;

import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import io.github.weimin96.springdocplus.gateway.controller.dto.SpringdocPlusUiConfig;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pwm
 */
@RestController
public class SpringdocPlusUiConfigController {

  private final SpringdocPlusGatewayProperties props;

  public SpringdocPlusUiConfigController(SpringdocPlusGatewayProperties props) {
    this.props = props;
  }

  @GetMapping(value = "/springdoc-plus-gateway/ui-config", produces = MediaType.APPLICATION_JSON_VALUE)
  public SpringdocPlusUiConfig config() {
    SpringdocPlusUiConfig cfg = new SpringdocPlusUiConfig();
    cfg.setTagsSorter(props.getTagsSorter());
    cfg.setOperationsSorter(props.getOperationsSorter());

    if (props.getAuth() != null) {
      cfg.setAuthEnabled(props.getAuth().isEnabled());
      cfg.setAuthHeaderName(props.getAuth().getHeaderName());
      cfg.setAuthDefaultPrefix(props.getAuth().getDefaultPrefix());
      cfg.setAuthPersist(props.getAuth().isPersist());
    }

    if (props.getBasic() != null) {
      cfg.setGatewayBasicEnabled(props.getBasic().isEnabled());
    }

    return cfg;
  }
}
