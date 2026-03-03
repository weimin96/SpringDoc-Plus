package io.github.weimin96.springdocplus.gateway.config;

import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import io.github.weimin96.springdocplus.gateway.controller.DocHtmlController;
import io.github.weimin96.springdocplus.gateway.controller.SpringdocPlusGatewayOpenApiController;
import io.github.weimin96.springdocplus.gateway.controller.SpringdocPlusUiConfigController;
import io.github.weimin96.springdocplus.gateway.discover.DiscoverGroupsService;
import io.github.weimin96.springdocplus.gateway.security.BasicAuthWebFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * @author pwm
 */
@Configuration
public class SpringdocPlusGatewayAutoConfiguration {

    @Bean
    public DiscoverGroupsService discoverGroupsService(
            SpringdocPlusGatewayProperties props,
            ObjectProvider<RouteDefinitionLocator> locatorProvider) {
        return new DiscoverGroupsService(props, locatorProvider.getIfAvailable());
    }

    @Bean
    public SpringdocPlusGatewayOpenApiController springdocPlusGatewayOpenApiController(
            SpringdocPlusGatewayProperties props,
            DiscoverGroupsService discoverGroupsService,
            ObjectProvider<org.springframework.cloud.client.discovery.DiscoveryClient> discoveryClientProvider) {
        return new SpringdocPlusGatewayOpenApiController(props, discoverGroupsService, discoveryClientProvider);
    }

    @Bean
    public DocHtmlController docHtmlController(ResourceLoader resourceLoader) {
        return new DocHtmlController(resourceLoader);
    }

    @Bean
    public SpringdocPlusUiConfigController springdocPlusUiConfigController(
            SpringdocPlusGatewayProperties props
    ) {
        return new SpringdocPlusUiConfigController(props);
    }

    @Bean
    public org.springframework.web.server.WebFilter springdocPlusBasicAuthWebFilter(SpringdocPlusGatewayProperties props) {
        return new BasicAuthWebFilter(props);
    }

    // 注意：不使用 WebFluxConfigurer 配置静态资源，因为可能与网关路由冲突
    // 静态资源由 DocHtmlController 直接处理
}
