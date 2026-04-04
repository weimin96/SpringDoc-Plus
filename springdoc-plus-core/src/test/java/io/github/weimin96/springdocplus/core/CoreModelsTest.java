package io.github.weimin96.springdocplus.core;

import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import io.github.weimin96.springdocplus.core.enums.OpenApiVersion;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.core.model.SpringdocPlusGatewayAuth;
import io.github.weimin96.springdocplus.core.model.SpringdocPlusGatewayHttpBasic;
import io.github.weimin96.springdocplus.core.model.SpringdocPlusUiConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoreModelsTest {

    @Test
    void enumsExposeExpectedValues() {
        assertThat(GatewayStrategy.values()).containsExactly(GatewayStrategy.MANUAL, GatewayStrategy.DISCOVER);
        assertThat(GroupOrderStrategy.values()).containsExactly(GroupOrderStrategy.alpha, GroupOrderStrategy.order);
        assertThat(OpenApiVersion.values()).containsExactly(OpenApiVersion.OPENAPI3);
    }

    @Test
    void gatewayRouteStoresAllFields() {
        GatewayRoute route = new GatewayRoute();
        route.setName("users");
        route.setServiceName("user-service");
        route.setUrl("/user-service/v3/api-docs");
        route.setContextPath("/user-service");
        route.setGroup("admin");
        route.setGroupNames(List.of("admin", "default"));
        route.setOrder(3);

        assertThat(route.getName()).isEqualTo("users");
        assertThat(route.getServiceName()).isEqualTo("user-service");
        assertThat(route.getUrl()).isEqualTo("/user-service/v3/api-docs");
        assertThat(route.getContextPath()).isEqualTo("/user-service");
        assertThat(route.getGroup()).isEqualTo("admin");
        assertThat(route.getGroupNames()).containsExactly("admin", "default");
        assertThat(route.getOrder()).isEqualTo(3);
    }

    @Test
    void authModelStoresAllFields() {
        SpringdocPlusGatewayAuth auth = new SpringdocPlusGatewayAuth();
        auth.setEnabled(false);
        auth.setHeaderName("X-Token");
        auth.setDefaultPrefix("Bearer");
        auth.setPersist(false);

        assertThat(auth.isEnabled()).isFalse();
        assertThat(auth.getHeaderName()).isEqualTo("X-Token");
        assertThat(auth.getDefaultPrefix()).isEqualTo("Bearer");
        assertThat(auth.isPersist()).isFalse();
    }

    @Test
    void basicModelStoresAllFields() {
        SpringdocPlusGatewayHttpBasic basic = new SpringdocPlusGatewayHttpBasic();
        basic.setEnabled(true);
        basic.setUsername("admin");
        basic.setPassword("secret");

        assertThat(basic.isEnabled()).isTrue();
        assertThat(basic.getUsername()).isEqualTo("admin");
        assertThat(basic.getPassword()).isEqualTo("secret");
    }

    @Test
    void uiConfigStoresAllFields() {
        SpringdocPlusUiConfig config = new SpringdocPlusUiConfig();
        config.setTagsSorter(GroupOrderStrategy.order);
        config.setOperationsSorter(GroupOrderStrategy.order);
        config.setTryItOutEnabled(false);
        config.setAuthEnabled(false);
        config.setAuthHeaderName("X-Auth");
        config.setAuthDefaultPrefix("Token");
        config.setAuthPersist(false);
        config.setGatewayBasicEnabled(true);

        assertThat(config.getTagsSorter()).isEqualTo(GroupOrderStrategy.order);
        assertThat(config.getOperationsSorter()).isEqualTo(GroupOrderStrategy.order);
        assertThat(config.isTryItOutEnabled()).isFalse();
        assertThat(config.isAuthEnabled()).isFalse();
        assertThat(config.getAuthHeaderName()).isEqualTo("X-Auth");
        assertThat(config.getAuthDefaultPrefix()).isEqualTo("Token");
        assertThat(config.isAuthPersist()).isFalse();
        assertThat(config.isGatewayBasicEnabled()).isTrue();
    }
}
