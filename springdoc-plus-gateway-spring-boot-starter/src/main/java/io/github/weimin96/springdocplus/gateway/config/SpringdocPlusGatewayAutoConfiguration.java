package io.github.weimin96.springdocplus.gateway.config;

import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import io.github.weimin96.springdocplus.gateway.controller.DocHtmlController;
import io.github.weimin96.springdocplus.gateway.controller.SpringdocPlusGatewayOpenApiController;
import io.github.weimin96.springdocplus.gateway.controller.SpringdocPlusUiConfigController;
import io.github.weimin96.springdocplus.gateway.discover.DiscoverGroupsService;
import io.github.weimin96.springdocplus.gateway.exception.GlobalExceptionHandler;
import io.github.weimin96.springdocplus.gateway.security.BasicAuthWebFilter;
import io.github.weimin96.springdocplus.gateway.security.SpringdocPlusSecurityHeadersWebFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * 网关自动配置类。
 * <p>
 * 用于配置网关聚合文档所需的 Bean，包括控制器、服务和过滤器等。
 *
 * @author pwm
 */
@Configuration
public class SpringdocPlusGatewayAutoConfiguration {

    /**
     * 无参构造器
     */
    public SpringdocPlusGatewayAutoConfiguration() {
    }

    /**
     * 创建分组服务 Bean
     *
     * @param props           网关配置属性
     * @param locatorProvider 网关路由定义定位器提供者
     * @return 分组服务实例
     */
    @Bean
    public DiscoverGroupsService discoverGroupsService(
            SpringdocPlusGatewayProperties props,
            ObjectProvider<RouteDefinitionLocator> locatorProvider) {
        return new DiscoverGroupsService(props, locatorProvider.getIfAvailable());
    }

    /**
     * 创建网关 OpenAPI 控制器 Bean
     *
     * @param props                  网关配置属性
     * @param discoverGroupsService 分组服务
     * @param discoveryClientProvider 服务发现客户端提供者
     * @return 网关 OpenAPI 控制器实例
     */
    @Bean
    public SpringdocPlusGatewayOpenApiController springdocPlusGatewayOpenApiController(
            SpringdocPlusGatewayProperties props,
            DiscoverGroupsService discoverGroupsService,
            ObjectProvider<org.springframework.cloud.client.discovery.DiscoveryClient> discoveryClientProvider,
            ObjectProvider<org.springframework.cloud.client.discovery.ReactiveDiscoveryClient> reactiveDiscoveryClientProvider) {
        return new SpringdocPlusGatewayOpenApiController(props, discoverGroupsService, discoveryClientProvider, reactiveDiscoveryClientProvider);
    }

    /**
     * 创建 DocHtml 控制器 Bean
     *
     * @param resourceLoader 资源加载器
     * @return DocHtml 控制器实例
     */
    @Bean
    public DocHtmlController docHtmlController(ResourceLoader resourceLoader) {
        return new DocHtmlController(resourceLoader);
    }

    /**
     * 创建 UI 配置控制器 Bean
     *
     * @param props 网关配置属性
     * @return UI 配置控制器实例
     */
    @Bean
    public SpringdocPlusUiConfigController springdocPlusUiConfigController(
            SpringdocPlusGatewayProperties props
    ) {
        return new SpringdocPlusUiConfigController(props);
    }

    /**
     * 创建 Basic 认证过滤器 Bean
     *
     * @param props 网关配置属性
     * @return Basic 认证过滤器实例
     */
    @Bean
    public org.springframework.web.server.WebFilter springdocPlusBasicAuthWebFilter(SpringdocPlusGatewayProperties props) {
        return new BasicAuthWebFilter(props);
    }

    @Bean
    public org.springframework.web.server.WebFilter springdocPlusSecurityHeadersWebFilter() {
        return new SpringdocPlusSecurityHeadersWebFilter();
    }

    @Bean
    public WebFluxConfigurer springdocPlusResourceConfigurer() {
        return new SpringdocPlusResourceConfiguration();
    }

    @Bean
    public GlobalExceptionHandler springdocPlusGlobalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
