package io.github.weimin96.springdocplus.gateway;

import io.github.weimin96.springdocplus.gateway.config.SpringdocPlusGatewayAutoConfiguration;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 网关 Starter 自动配置类。
 * <p>
 * 当 classpath 中存在 Spring Cloud Gateway 的 {@code RouteDefinition} 类时自动配置，
 * 并根据配置属性 {@code springdoc-plus.gateway.enabled} 决定是否启用。
 *
 * @author pwm
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.gateway.route.RouteDefinition")
@ConditionalOnProperty(prefix = SpringdocPlusGatewayProperties.PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SpringdocPlusGatewayProperties.class)
@Import(SpringdocPlusGatewayAutoConfiguration.class)
public class SpringdocPlusGatewayStarterAutoConfiguration {

    /**
     * 无参构造器
     */
    public SpringdocPlusGatewayStarterAutoConfiguration() {
    }
}
