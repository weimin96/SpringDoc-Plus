package io.github.weimin96.springdocplus.openapi3;

import io.github.weimin96.springdocplus.openapi3.controller.DocHtmlController;
import io.github.weimin96.springdocplus.openapi3.controller.SingleOpenApiGroupsController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

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
     * @return SingleOpenApiGroupsController 实例
     */
    @Bean
    public SingleOpenApiGroupsController singleOpenApiGroupsController() {
        return new SingleOpenApiGroupsController();
    }

    /**
     * 无参构造器
     */
    public SpringdocPlusOpenApi3StarterAutoConfiguration() {
    }
}
