package io.github.weimin96.springdocplus.openapi3;

import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Spring WebFlux 单服务 OpenAPI3 自动配置。
 *
 * @author pwm
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springdoc.webflux.ui.SwaggerConfig")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(prefix = SpringdocPlusOpenApi3Properties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(SpringdocPlusOpenApi3CommonAutoConfiguration.class)
public class SpringdocPlusWebFluxAutoConfiguration {

    /**
     * WebFlux 模式尚未提供响应式 Basic 过滤器，启动期失败比静默放行更安全。
     *
     * @return 不会返回
     */
    @Bean
    @ConditionalOnProperty(prefix = "springdoc-plus.openapi3.basic", name = "enabled", havingValue = "true")
    public Object springdocPlusUnsupportedWebFluxBasicAuth() {
        throw new IllegalStateException("springdoc-plus.openapi3.basic.enabled=true 暂不支持 WebFlux 单服务模式");
    }
}
