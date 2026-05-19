package io.github.weimin96.springdocplus.gateway.proxy;

import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiSpecProxyServiceTest {

    @Test
    void proxiesRegisteredRouteAndAddsCacheHeaders() {
        AtomicReference<URI> requestedUri = new AtomicReference<>();
        OpenApiSpecProxyService service = new OpenApiSpecProxyService(new SpringdocPlusGatewayProperties(), webClient(requestedUri, new AtomicInteger()));

        StepVerifier.create(service.proxy(exchange(null), List.of(route("user-service", null, "/user-service/v3/api-docs?group=admin")), "user-service", "default"))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(new String(response.getBody(), StandardCharsets.UTF_8)).contains("\"openapi\"");
                    assertThat(response.getHeaders().getETag()).isNotBlank();
                    assertThat(response.getHeaders().getFirst("X-Springdoc-Plus-Document-Source"))
                            .isEqualTo("http://gateway.local/user-service/v3/api-docs?group=admin");
                })
                .verifyComplete();

        assertThat(requestedUri.get()).hasToString("http://gateway.local/user-service/v3/api-docs?group=admin");
    }

    @Test
    void returnsNotModifiedWhenCachedEtagMatches() {
        AtomicInteger calls = new AtomicInteger();
        OpenApiSpecProxyService service = new OpenApiSpecProxyService(new SpringdocPlusGatewayProperties(), webClient(new AtomicReference<>(), calls));
        GatewayRoute route = route("user-service", null, "/user-service/v3/api-docs");

        String etag = service.proxy(exchange(null), List.of(route), "user-service", "default")
                .block()
                .getHeaders()
                .getETag();

        StepVerifier.create(service.proxy(exchange(etag), List.of(route), "user-service", "default"))
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED))
                .verifyComplete();

        assertThat(calls).hasValue(1);
    }

    @Test
    void rejectsUnknownServiceAndOversizedDocument() {
        SpringdocPlusGatewayProperties props = new SpringdocPlusGatewayProperties();
        props.getProxy().setMaxDocumentBytes(4);
        OpenApiSpecProxyService service = new OpenApiSpecProxyService(props, WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("{\"openapi\":\"3\"}")
                        .build()))
                .build());

        StepVerifier.create(service.proxy(exchange(null), List.of(), "missing", "default"))
                .expectError(ResponseStatusException.class)
                .verify();

        StepVerifier.create(service.proxy(exchange(null), List.of(route("user-service", null, "/user-service/v3/api-docs")), "user-service", "default"))
                .expectErrorMatches(error -> error instanceof ResponseStatusException
                        && ((ResponseStatusException) error).getStatusCode().isSameCodeAs(HttpStatus.PAYLOAD_TOO_LARGE))
                .verify();
    }

    private WebClient webClient(AtomicReference<URI> requestedUri, AtomicInteger calls) {
        return WebClient.builder()
                .exchangeFunction(request -> {
                    requestedUri.set(request.url());
                    calls.incrementAndGet();
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body("{\"openapi\":\"3.0.1\"}")
                            .build());
                })
                .build();
    }

    private MockServerWebExchange exchange(String etag) {
        MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest
                .get("http://gateway.local/springdoc-plus-gateway/openapi/spec?service=user-service");
        if (etag != null) {
            request.header(HttpHeaders.IF_NONE_MATCH, etag);
        }
        return MockServerWebExchange.from(request.build());
    }

    private GatewayRoute route(String service, String group, String url) {
        GatewayRoute route = new GatewayRoute();
        route.setServiceName(service);
        route.setGroup(group);
        route.setUrl(url);
        return route;
    }
}
