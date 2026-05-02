package io.github.weimin96.springdocplus.gateway.controller;

import io.github.weimin96.springdocplus.core.model.SpringdocPlusUiConfig;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UI 配置控制器。
 * <p>
 * 提供 UI 所需的配置信息接口，包括排序策略、认证设置等。
 *
 * @author pwm
 */
@RestController
public class SpringdocPlusUiConfigController {

  private final SpringdocPlusGatewayProperties props;

  /**
   * 构造器
   *
   * @param props 网关配置属性
   */
  public SpringdocPlusUiConfigController(SpringdocPlusGatewayProperties props) {
    this.props = props;
  }

  /**
   * 获取 UI 配置
   *
   * @return UI 配置信息
   */
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
      if (props.getAuth().getOauth2() != null) {
        cfg.setOauth2Enabled(props.getAuth().getOauth2().isEnabled());
        cfg.setOauth2TokenUrl(props.getAuth().getOauth2().getTokenUrl());
        cfg.setOauth2ClientId(props.getAuth().getOauth2().getClientId());
        cfg.setOauth2Scope(props.getAuth().getOauth2().getScope());
        cfg.setOauth2GrantType(props.getAuth().getOauth2().getGrantType());
      }
    }

    if (props.getBasic() != null) {
      cfg.setGatewayBasicEnabled(props.getBasic().isEnabled());
    }

    return cfg;
  }
}
