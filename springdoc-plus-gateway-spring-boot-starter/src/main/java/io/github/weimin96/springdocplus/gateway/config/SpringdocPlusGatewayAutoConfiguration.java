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
    public DocHtmlController docHtmlController() {
        return new DocHtmlController();
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
}
