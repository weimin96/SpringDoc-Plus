package io.github.weimin96.springdocplus.gateway.controller;

import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.gateway.discover.DiscoverGroupsService;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import io.github.weimin96.springdocplus.gateway.proxy.OpenApiSpecProxyService;
import io.github.weimin96.springdocplus.gateway.security.SpringdocPlusSecurityHeadersWebFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GatewayControllersTest {

    @Test
    void uiConfigReflectsGatewayProperties() {
        SpringdocPlusGatewayProperties props = new SpringdocPlusGatewayProperties();
        props.setTagsSorter(GroupOrderStrategy.order);
        props.setOperationsSorter(GroupOrderStrategy.order);
        props.getAuth().setEnabled(false);
        props.getAuth().setHeaderName("X-Token");
        props.getAuth().setDefaultPrefix("Bearer");
        props.getAuth().setPersist(false);
        props.getBasic().setEnabled(true);

        SpringdocPlusUiConfigController controller = new SpringdocPlusUiConfigController(props);
        var config = controller.config();

        assertThat(config.getTagsSorter()).isEqualTo(GroupOrderStrategy.order);
        assertThat(config.getOperationsSorter()).isEqualTo(GroupOrderStrategy.order);
        assertThat(config.isAuthEnabled()).isFalse();
        assertThat(config.getAuthHeaderName()).isEqualTo("X-Token");
        assertThat(config.getAuthDefaultPrefix()).isEqualTo("Bearer");
        assertThat(config.isAuthPersist()).isFalse();
        assertThat(config.isGatewayBasicEnabled()).isTrue();
    }

    @Test
    void gatewayGroupsUsesReactiveDiscoveryClientWhenAvailable() {
        SpringdocPlusGatewayProperties props = new SpringdocPlusGatewayProperties();
        props.setStrategy(GatewayStrategy.DISCOVER);
        props.getDiscover().setEnabled(true);

        DiscoverGroupsService service = mock(DiscoverGroupsService.class);
        GatewayRoute route = new GatewayRoute();
        route.setName("users");
        when(service.getGroupsReactive(Optional.of(List.of("user-service")))).thenReturn(reactor.core.publisher.Mono.just(List.of(route)));

        ReactiveDiscoveryClient reactiveDiscoveryClient = mock(ReactiveDiscoveryClient.class);
        when(reactiveDiscoveryClient.getServices()).thenReturn(Flux.just("user-service"));

        SpringdocPlusGatewayOpenApiController controller = new SpringdocPlusGatewayOpenApiController(
                props,
                service,
                mock(OpenApiSpecProxyService.class),
                objectProvider(null),
                objectProvider(reactiveDiscoveryClient)
        );

        var response = controller.groups().block();

        assertThat(response).isNotNull();
        assertThat(response.groups()).containsExactly(route);
        verify(service).getGroupsReactive(Optional.of(List.of("user-service")));
    }

    @Test
    void gatewayGroupsFallsBackToBlockingDiscoveryClient() {
        SpringdocPlusGatewayProperties props = new SpringdocPlusGatewayProperties();
        props.setStrategy(GatewayStrategy.DISCOVER);
        props.getDiscover().setEnabled(true);

        DiscoverGroupsService service = mock(DiscoverGroupsService.class);
        when(service.getGroupsReactive(Optional.of(List.of("order-service")))).thenReturn(reactor.core.publisher.Mono.just(List.of()));

        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getServices()).thenReturn(List.of("order-service"));

        SpringdocPlusGatewayOpenApiController controller = new SpringdocPlusGatewayOpenApiController(
                props,
                service,
                mock(OpenApiSpecProxyService.class),
                objectProvider(discoveryClient),
                objectProvider(null)
        );

        controller.groups().block();
        verify(service).getGroupsReactive(Optional.of(List.of("order-service")));
    }

    @Test
    void gatewayGroupsReturnsEmptyWhenDiscoveryDisabled() {
        SpringdocPlusGatewayProperties props = new SpringdocPlusGatewayProperties();
        DiscoverGroupsService service = mock(DiscoverGroupsService.class);
        when(service.getGroupsReactive(Optional.empty())).thenReturn(reactor.core.publisher.Mono.just(List.of()));

        SpringdocPlusGatewayOpenApiController controller = new SpringdocPlusGatewayOpenApiController(
                props,
                service,
                mock(OpenApiSpecProxyService.class),
                objectProvider(null),
                objectProvider(null)
        );

        var response = controller.groups().block();
        assertThat(response.groups()).isEmpty();
        verify(service).getGroupsReactive(Optional.empty());
    }

    @Test
    void docHtmlControllerValidatesAndResolvesAssets() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        Resource resource = new ByteArrayResource("demo".getBytes(StandardCharsets.UTF_8));
        when(resourceLoader.getResource(any())).thenReturn(resource);

        DocHtmlController controller = new DocHtmlController(resourceLoader);

        ResponseEntity<Resource> html = controller.docHtml().block();
        assertThat(html.getBody()).isSameAs(resource);
        assertThat(html.getHeaders().getFirst("Cache-Control")).isEqualTo("no-cache");
    }

    @Test
    void securityHeadersApplyOnlyToDocumentationResources() {
        SpringdocPlusSecurityHeadersWebFilter filter = new SpringdocPlusSecurityHeadersWebFilter();
        MockServerWebExchange docExchange = MockServerWebExchange.from(MockServerHttpRequest.get("/doc.html").build());
        WebFilterChain chain = exchange -> exchange.getResponse().setComplete();

        filter.filter(docExchange, chain).block();

        assertThat(docExchange.getResponse().getHeaders().getFirst("Content-Security-Policy")).contains("frame-ancestors 'self'");
        assertThat(docExchange.getResponse().getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(docExchange.getResponse().getHeaders().getFirst("Referrer-Policy")).isEqualTo("strict-origin-when-cross-origin");
        assertThat(docExchange.getResponse().getHeaders().getFirst("X-Frame-Options")).isEqualTo("SAMEORIGIN");

        MockServerWebExchange apiExchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/users").build());
        filter.filter(apiExchange, chain).block();
        assertThat(apiExchange.getResponse().getHeaders().getFirst("Content-Security-Policy")).isNull();
    }

    private static <T> ObjectProvider<T> objectProvider(T value) {
        return new ObjectProvider<>() {
            @Override
            public T getObject(Object... args) {
                return value;
            }

            @Override
            public T getIfAvailable() {
                return value;
            }

            @Override
            public T getIfUnique() {
                return value;
            }

            @Override
            public T getObject() {
                return value;
            }
        };
    }
}
