package io.github.weimin96.springdocplus.openapi3;

import io.github.weimin96.springdocplus.openapi3.controller.DocHtmlController;
import io.github.weimin96.springdocplus.openapi3.controller.SingleOpenApiGroupsController;
import io.github.weimin96.springdocplus.openapi3.controller.SingleOpenApiUiConfigController;
import io.github.weimin96.springdocplus.openapi3.customizer.DocOrderOperationCustomizer;
import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * 单服务 OpenAPI3 公共自动配置。
 *
 * @author pwm
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SpringdocPlusOpenApi3Properties.class)
public class SpringdocPlusOpenApi3CommonAutoConfiguration {

    /**
     * 创建文档入口控制器。
     *
     * @param resourceLoader 资源加载器
     * @return 文档入口控制器
     */
    @Bean
    @ConditionalOnMissingBean
    public DocHtmlController docHtmlController(ResourceLoader resourceLoader) {
        return new DocHtmlController(resourceLoader);
    }

    /**
     * 创建单服务分组控制器。
     *
     * @param props 单服务配置属性
     * @return 单服务分组控制器
     */
    @Bean
    @ConditionalOnMissingBean
    public SingleOpenApiGroupsController singleOpenApiGroupsController(SpringdocPlusOpenApi3Properties props) {
        return new SingleOpenApiGroupsController(props);
    }

    /**
     * 创建单服务 UI 配置控制器。
     *
     * @param props 单服务配置属性
     * @return 单服务 UI 配置控制器
     */
    @Bean
    @ConditionalOnMissingBean
    public SingleOpenApiUiConfigController singleOpenApiUiConfigController(SpringdocPlusOpenApi3Properties props) {
        return new SingleOpenApiUiConfigController(props);
    }

    /**
     * 创建 DocOrder 注解转换器。
     *
     * @return OpenAPI Operation 自定义器
     */
    @Bean
    @ConditionalOnMissingBean(name = "springdocPlusDocOrderOperationCustomizer")
    public OperationCustomizer springdocPlusDocOrderOperationCustomizer() {
        return new DocOrderOperationCustomizer();
    }
}
