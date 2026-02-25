package io.github.weimin96.springdocplus.openapi3;

import io.github.weimin96.springdocplus.openapi3.controller.DocHtmlController;
import io.github.weimin96.springdocplus.openapi3.controller.SingleOpenApiGroupsController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author pwm
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springdoc.webmvc.ui.SwaggerConfig")
@ConditionalOnProperty(prefix = "springdoc-plus.openapi3", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SpringdocPlusOpenApi3StarterAutoConfiguration {

    @Bean
    public DocHtmlController docHtmlController() {
        return new DocHtmlController();
    }

    @Bean
    public SingleOpenApiGroupsController singleOpenApiGroupsController() {
        return new SingleOpenApiGroupsController();
    }
}
