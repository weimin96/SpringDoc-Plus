package io.github.weimin96.springdocplus.gateway.controller;

import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.gateway.discover.DiscoverGroupsService;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

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

        ResponseEntity<Resource> js = controller.uiAsset("index.js").block();
        assertThat(js.getHeaders().getFirst("Cache-Control")).isEqualTo("no-cache");

        ResponseEntity<Resource> invalid = controller.uiAsset("../secret").block();
        assertThat(invalid.getStatusCode().value()).isEqualTo(400);

        ResponseEntity<Resource> root = controller.uiRootAsset("favicon.svg").block();
        assertThat(root.getHeaders().getContentType().toString()).contains("image/svg+xml");

        ResponseEntity<Resource> css = controller.uiAsset("styles.css").block();
        assertThat(css.getHeaders().getContentType().toString()).contains("text/css");

        ResponseEntity<Resource> png = controller.uiRootAsset("logo.png").block();
        assertThat(png.getHeaders().getContentType().toString()).contains("image/png");

        ResponseEntity<Resource> docx = controller.uiDocAsset("模板.docx").block();
        assertThat(docx.getHeaders().getContentType().toString()).contains("wordprocessingml.document");

        ResponseEntity<Resource> ico = controller.uiRootAsset("favicon.ico").block();
        assertThat(ico.getHeaders().getContentType().toString()).contains("image/x-icon");

        ResponseEntity<Resource> binary = controller.uiAsset("font.woff").block();
        assertThat(binary.getHeaders().getContentType().toString()).contains("application/octet-stream");

        ResponseEntity<Resource> invalidRoot = controller.uiRootAsset("..\\secret").block();
        assertThat(invalidRoot.getStatusCode().value()).isEqualTo(400);

        ResponseEntity<Resource> invalidDoc = controller.uiDocAsset("../模板.docx").block();
        assertThat(invalidDoc.getStatusCode().value()).isEqualTo(400);
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
