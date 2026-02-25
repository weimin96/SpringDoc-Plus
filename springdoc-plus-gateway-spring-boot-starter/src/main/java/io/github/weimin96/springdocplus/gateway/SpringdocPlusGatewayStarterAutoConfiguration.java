package io.github.weimin96.springdocplus.gateway;

import io.github.weimin96.springdocplus.gateway.config.SpringdocPlusGatewayAutoConfiguration;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * @author pwm
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.gateway.route.RouteDefinition")
@ConditionalOnProperty(prefix = SpringdocPlusGatewayProperties.PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SpringdocPlusGatewayProperties.class)
@Import(SpringdocPlusGatewayAutoConfiguration.class)
public class SpringdocPlusGatewayStarterAutoConfiguration {
}
