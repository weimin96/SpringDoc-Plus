package io.github.weimin96.springdocplus.openapi3;

import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import io.github.weimin96.springdocplus.openapi3.security.BasicAuthFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * Spring MVC 单服务 OpenAPI3 自动配置。
 *
 * @author pwm
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springdoc.webmvc.ui.SwaggerConfig")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = SpringdocPlusOpenApi3Properties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(SpringdocPlusOpenApi3CommonAutoConfiguration.class)
public class SpringdocPlusMvcAutoConfiguration {

    /**
     * 注册 Servlet Basic 过滤器。
     *
     * @param props 单服务配置属性
     * @return Servlet 过滤器注册信息
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
}
