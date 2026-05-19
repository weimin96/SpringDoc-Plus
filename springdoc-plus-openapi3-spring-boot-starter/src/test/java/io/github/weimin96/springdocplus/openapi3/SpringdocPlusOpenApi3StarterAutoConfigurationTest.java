package io.github.weimin96.springdocplus.openapi3;

import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import io.github.weimin96.springdocplus.openapi3.controller.DocHtmlController;
import io.github.weimin96.springdocplus.openapi3.controller.SingleOpenApiGroupsController;
import io.github.weimin96.springdocplus.openapi3.controller.SingleOpenApiUiConfigController;
import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SpringdocPlusOpenApi3StarterAutoConfigurationTest {

    private final WebApplicationContextRunner mvcContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SpringdocPlusMvcAutoConfiguration.class,
                    SpringdocPlusWebFluxAutoConfiguration.class));

    @Test
    void registersStarterBeansWhenPropertyMissing() {
        mvcContextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(DocHtmlController.class);
                    assertThat(context).hasSingleBean(SingleOpenApiGroupsController.class);
                    assertThat(context).hasSingleBean(SingleOpenApiUiConfigController.class);

                    SpringdocPlusOpenApi3Properties properties = context.getBean(SpringdocPlusOpenApi3Properties.class);
                    assertThat(properties.isEnabled()).isTrue();
                });
    }

    @Test
    void registersStarterBeansWhenExplicitlyEnabled() {
        mvcContextRunner
                .withPropertyValues(
                        "springdoc-plus.openapi3.enabled=true",
                        "springdoc-plus.openapi3.tags-sorter=order",
                        "springdoc-plus.openapi3.operations-sorter=order",
                        "springdoc-plus.openapi3.auth.enabled=false",
                        "springdoc-plus.openapi3.basic.enabled=true",
                        "springdoc-plus.openapi3.groups[0].name=admin",
                        "springdoc-plus.openapi3.groups[0].url=/v3/api-docs?group=admin",
                        "springdoc-plus.openapi3.groups[0].context-path=/api",
                        "springdoc-plus.openapi3.groups[0].order=1"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(DocHtmlController.class);
                    assertThat(context).hasSingleBean(SingleOpenApiGroupsController.class);
                    assertThat(context).hasSingleBean(SingleOpenApiUiConfigController.class);

                    SpringdocPlusOpenApi3Properties properties = context.getBean(SpringdocPlusOpenApi3Properties.class);
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getTagsSorter()).isEqualTo(GroupOrderStrategy.order);
                    assertThat(properties.getOperationsSorter()).isEqualTo(GroupOrderStrategy.order);
                    assertThat(properties.getAuth().isEnabled()).isFalse();
                    assertThat(properties.getBasic().isEnabled()).isTrue();
                    assertThat(properties.getGroups()).hasSize(1);
                    assertThat(properties.getGroups().get(0).getName()).isEqualTo("admin");
                });
    }

    @Test
    void backsOffWhenDisabled() {
        mvcContextRunner
                .withPropertyValues("springdoc-plus.openapi3.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DocHtmlController.class);
                    assertThat(context).doesNotHaveBean(SingleOpenApiGroupsController.class);
                    assertThat(context).doesNotHaveBean(SingleOpenApiUiConfigController.class);
                });
    }

    @Test
    void backsOffWhenMvcSwaggerConfigMissing() {
        mvcContextRunner
                .withClassLoader(new FilteredClassLoader("org.springdoc.webmvc.ui.SwaggerConfig"))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DocHtmlController.class);
                    assertThat(context).doesNotHaveBean(SingleOpenApiGroupsController.class);
                    assertThat(context).doesNotHaveBean(SingleOpenApiUiConfigController.class);
                });
    }

    @Test
    void backsOffOutsideWebApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        SpringdocPlusMvcAutoConfiguration.class,
                        SpringdocPlusWebFluxAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DocHtmlController.class);
                    assertThat(context).doesNotHaveBean(SingleOpenApiGroupsController.class);
                    assertThat(context).doesNotHaveBean(SingleOpenApiUiConfigController.class);
                });
    }
}
