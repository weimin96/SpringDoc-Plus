package io.github.weimin96.springdocplus.openapi3;

import io.github.weimin96.springdocplus.openapi3.controller.DocHtmlController;
import io.github.weimin96.springdocplus.openapi3.controller.SingleOpenApiGroupsController;
import io.github.weimin96.springdocplus.openapi3.controller.SingleOpenApiUiConfigController;
import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import io.github.weimin96.springdocplus.openapi3.security.BasicAuthFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * OpenAPI3 Starter 自动配置类。
 * <p>
 * 当 classpath 中存在 springdoc 的 SwaggerConfig 类时自动配置，
 * 并根据配置属性 springdoc-plus.openapi3.enabled 决定是否启用（默认为 true）。
 *
 * @author pwm
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springdoc.webmvc.ui.SwaggerConfig")
@ConditionalOnProperty(prefix = "springdoc-plus.openapi3", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpringdocPlusOpenApi3Properties.class)
public class SpringdocPlusOpenApi3StarterAutoConfiguration {

    /**
     * 创建 DocHtmlController Bean
     *
     * @return DocHtmlController 实例
     */
    @Bean
    public DocHtmlController docHtmlController() {
        return new DocHtmlController();
    }

    /**
     * 创建 SingleOpenApiGroupsController Bean
     *
     * @param props 单服务配置属性
     * @return SingleOpenApiGroupsController 实例
     */
    @Bean
    public SingleOpenApiGroupsController singleOpenApiGroupsController(SpringdocPlusOpenApi3Properties props) {
        return new SingleOpenApiGroupsController(props);
    }

    /**
     * 创建 SingleOpenApiUiConfigController Bean
     *
     * @param props 单服务配置属性
     * @return SingleOpenApiUiConfigController 实例
     */
    @Bean
    public SingleOpenApiUiConfigController singleOpenApiUiConfigController(SpringdocPlusOpenApi3Properties props) {
        return new SingleOpenApiUiConfigController(props);
    }

    /**
     * 注册 Basic Auth 过滤器（Servlet 版），与 Gateway 模块的 BasicAuthWebFilter 对齐。
     * <p>
     * 仅在配置了 {@code springdoc-plus.openapi3.basic.enabled=true} 时生效。
     * 过滤器优先级设置为最高，确保在 Spring Security 之前执行鉴权，
     * 同时避免影响业务接口路由。
     *
     * @param props 单服务配置属性
     * @return FilterRegistrationBean 实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "springdoc-plus.openapi3.basic", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<BasicAuthFilter> springdocPlusBasicAuthFilter(
            SpringdocPlusOpenApi3Properties props) {
        FilterRegistrationBean<BasicAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new BasicAuthFilter(props));
        registration.addUrlPatterns("/doc.html", "/springdoc-plus-ui/*", "/springdoc-plus-gateway/*");
        registration.setName("springdocPlusBasicAuthFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * 无参构造器
     */
    public SpringdocPlusOpenApi3StarterAutoConfiguration() {
    }
}
