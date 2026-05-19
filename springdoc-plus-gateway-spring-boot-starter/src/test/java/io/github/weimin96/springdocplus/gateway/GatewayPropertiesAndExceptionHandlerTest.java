package io.github.weimin96.springdocplus.gateway;

import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import io.github.weimin96.springdocplus.gateway.exception.GlobalExceptionHandler;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayPropertiesAndExceptionHandlerTest {

    @Test
    void propertiesExposeDefaultsAndNestedValues() {
        SpringdocPlusGatewayProperties properties = new SpringdocPlusGatewayProperties();
        properties.setEnabled(true);
        properties.setStrategy(GatewayStrategy.DISCOVER);
        properties.setTagsSorter(GroupOrderStrategy.order);
        properties.setOperationsSorter(GroupOrderStrategy.order);
        properties.getBasic().setEnabled(true);
        properties.getBasic().setUsername("admin");
        properties.getBasic().setPassword("secret");
        properties.getAuth().setEnabled(false);
        properties.getDiscover().setEnabled(true);
        properties.getDiscover().setExcludedServices(Set.of("ignore-service"));
        properties.getDiscover().getCache().setTtl(Duration.ofSeconds(30));
        properties.getDiscover().getCache().setMaximumSize(100);
        properties.getDiscover().setTimeout(Duration.ofSeconds(3));
        properties.getProxy().setEnabled(true);
        properties.getProxy().setTimeout(Duration.ofSeconds(2));
        properties.getProxy().getCache().setTtl(Duration.ofSeconds(20));
        properties.getProxy().getCache().setMaximumSize(50);
        properties.getProxy().setMaxDocumentBytes(1024);

        SpringdocPlusGatewayProperties.ServiceConfig serviceConfig = new SpringdocPlusGatewayProperties.ServiceConfig();
        serviceConfig.setOrder(2);
        serviceConfig.setGroupName("users");
        serviceConfig.setContextPath("/api/users");
        serviceConfig.setGroupNames(List.of("admin"));
        properties.getDiscover().getServiceConfig().put("user-service", serviceConfig);

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getStrategy()).isEqualTo(GatewayStrategy.DISCOVER);
        assertThat(properties.getTagsSorter()).isEqualTo(GroupOrderStrategy.order);
        assertThat(properties.getOperationsSorter()).isEqualTo(GroupOrderStrategy.order);
        assertThat(properties.getBasic().isEnabled()).isTrue();
        assertThat(properties.getBasic().getUsername()).isEqualTo("admin");
        assertThat(properties.getAuth().isEnabled()).isFalse();
        assertThat(properties.getDiscover().isEnabled()).isTrue();
        assertThat(properties.getDiscover().getExcludedServices()).contains("ignore-service");
        assertThat(properties.getDiscover().getCache().getTtl()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getDiscover().getCache().getMaximumSize()).isEqualTo(100);
        assertThat(properties.getDiscover().getTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(properties.getProxy().isEnabled()).isTrue();
        assertThat(properties.getProxy().getTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(properties.getProxy().getCache().getTtl()).isEqualTo(Duration.ofSeconds(20));
        assertThat(properties.getProxy().getCache().getMaximumSize()).isEqualTo(50);
        assertThat(properties.getProxy().getMaxDocumentBytes()).isEqualTo(1024);
        assertThat(properties.getDiscover().getServiceConfig().get("user-service").getContextPath()).isEqualTo("/api/users");
    }

    @Test
    void exceptionHandlerWritesStructuredErrorForResponseStatusException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/boom").build());

        StepVerifier.create(handler.handle(exchange, new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad \"request\"")))
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("\"code\":400");
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("\\\"request\\\"");
    }

    @Test
    void exceptionHandlerUsesInternalServerErrorForGenericExceptions() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/boom").build());

        StepVerifier.create(handler.handle(exchange, new IllegalStateException("failed")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("failed");
    }

    @Test
    void exceptionHandlerPassesThroughCommittedResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/committed").build());
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().setComplete().block();

        StepVerifier.create(handler.handle(exchange, new RuntimeException("boom")))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void exceptionHandlerUsesCauseMessageAndEscapesNulls() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/cause").build());

        RuntimeException exception = new RuntimeException((String) null);
        exception.initCause(new IllegalArgumentException("bad\ninput"));

        StepVerifier.create(handler.handle(exchange, exception))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("bad\\ninput");
    }
}
