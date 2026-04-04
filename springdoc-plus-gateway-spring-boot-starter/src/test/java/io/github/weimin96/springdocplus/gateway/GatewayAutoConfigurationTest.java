package io.github.weimin96.springdocplus.gateway;

import io.github.weimin96.springdocplus.gateway.controller.DocHtmlController;
import io.github.weimin96.springdocplus.gateway.controller.SpringdocPlusGatewayOpenApiController;
import io.github.weimin96.springdocplus.gateway.controller.SpringdocPlusUiConfigController;
import io.github.weimin96.springdocplus.gateway.discover.DiscoverGroupsService;
import io.github.weimin96.springdocplus.gateway.exception.GlobalExceptionHandler;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(RouteDefinitionLocator.class, () -> () -> reactor.core.publisher.Flux.empty())
            .withConfiguration(AutoConfigurations.of(SpringdocPlusGatewayStarterAutoConfiguration.class));

    @Test
    void starterRegistersBeansWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "springdoc-plus.gateway.enabled=true",
                        "springdoc-plus.gateway.tags-sorter=order",
                        "springdoc-plus.gateway.operations-sorter=order",
                        "springdoc-plus.gateway.auth.enabled=false",
                        "springdoc-plus.gateway.basic.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(SpringdocPlusGatewayProperties.class);
                    assertThat(context).hasSingleBean(DiscoverGroupsService.class);
                    assertThat(context).hasSingleBean(SpringdocPlusGatewayOpenApiController.class);
                    assertThat(context).hasSingleBean(SpringdocPlusUiConfigController.class);
                    assertThat(context).hasSingleBean(DocHtmlController.class);
                    assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
                });
    }

    @Test
    void starterBacksOffWhenDisabled() {
        contextRunner
                .withPropertyValues("springdoc-plus.gateway.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SpringdocPlusGatewayProperties.class);
                    assertThat(context).doesNotHaveBean(DiscoverGroupsService.class);
                });
    }
}
