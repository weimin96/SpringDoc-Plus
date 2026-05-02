package io.github.weimin96.springdocplus.gateway.security;

import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BasicAuthWebFilter 单元测试
 *
 * @author pwm
 */
class BasicAuthWebFilterTest {

    private SpringdocPlusGatewayProperties props;
    private BasicAuthWebFilter filter;

    @BeforeEach
    void setUp() {
        props = new SpringdocPlusGatewayProperties();
        // 默认启用 Basic 认证
        props.getBasic().setEnabled(true);
        props.getBasic().setUsername("admin");
        props.getBasic().setPassword("123456");
        filter = new BasicAuthWebFilter(props);
    }

    /**
     * 测试未启用 Basic 认证时直接放行
     */
    @Test
    void testNoBasicAuth_disabled() {
        props.getBasic().setEnabled(false);

        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain -> reactor.core.publisher.Mono.empty()))
                .verifyComplete();

        // 验证未设置 401 状态
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    /**
     * 测试无需保护的路径直接放行
     */
    @Test
    void testNoProtectionPath_passThrough() {
        // /other 不在保护范围内
        MockServerHttpRequest request = MockServerHttpRequest.get("/other").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain -> reactor.core.publisher.Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    /**
     * 测试无 Authorization header 时返回 401
     */
    @Test
    void testNoAuthHeader_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain -> reactor.core.publisher.Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE))
                .isEqualTo("Basic realm=\"springdoc-plus\"");
    }

    /**
     * 测试无效 Authorization header 格式时返回 401
     */
    @Test
    void testInvalidAuthFormat_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html")
                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain -> reactor.core.publisher.Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * 测试无效 Base64 编码时返回 401
     */
    @Test
    void testInvalidBase64_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html")
                .header(HttpHeaders.AUTHORIZATION, "Basic !@#$%^&*()")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain -> reactor.core.publisher.Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * 测试错误的用户名密码时返回 401
     */
    @Test
    void testWrongCredentials_returns401() {
        String encoded = Base64.getEncoder().encodeToString("wrong:credentials".getBytes());
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain -> reactor.core.publisher.Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * 测试正确的用户名密码时放行
     */
    @Test
    void testCorrectCredentials_passThrough() {
        String encoded = Base64.getEncoder().encodeToString("admin:123456".getBytes());
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // 标记是否放行
        final boolean[] passed = {false };
        StepVerifier.create(filter.filter(exchange, chain -> {
            passed[0] = true;
            return reactor.core.publisher.Mono.empty();
        }))
                .verifyComplete();

        assertThat(passed[0]).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    /**
     * 测试 {bcrypt} 前缀密码，避免生产配置必须保存明文密码。
     */
    @Test
    void testBcryptPassword_passThrough() {
        String encodedPassword = new BCryptPasswordEncoder().encode("123456");
        props.getBasic().setPassword("{bcrypt}" + encodedPassword);

        String encoded = Base64.getEncoder().encodeToString("admin:123456".getBytes());
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        final boolean[] passed = {false };
        StepVerifier.create(filter.filter(exchange, chain -> {
            passed[0] = true;
            return reactor.core.publisher.Mono.empty();
        }))
                .verifyComplete();

        assertThat(passed[0]).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    /**
     * 测试空密码情况
     */
    @Test
    void testEmptyPassword() {
        props.getBasic().setPassword("");

        String encoded = Base64.getEncoder().encodeToString("admin:".getBytes());
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain -> reactor.core.publisher.Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * 测试空用户名情况
     */
    @Test
    void testEmptyUsername() {
        props.getBasic().setUsername("");

        String encoded = Base64.getEncoder().encodeToString(":123456".getBytes());
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain -> reactor.core.publisher.Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * 测试 null 用户名和密码
     */
    @Test
    void testNullUsernameAndPassword() {
        props.getBasic().setUsername(null);
        props.getBasic().setPassword(null);

        // username:password -> ::(中间是冒号)
        String encoded = Base64.getEncoder().encodeToString(":".getBytes());
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain -> reactor.core.publisher.Mono.empty()))
                .verifyComplete();

        // 空用户名密码应该不匹配任何非空输入
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * 测试 /springdoc-plus-ui/** 路径受保护
     */
    @Test
    void testUiPath_protected() {
        String encoded = Base64.getEncoder().encodeToString("admin:123456".getBytes());
        MockServerHttpRequest request = MockServerHttpRequest.get("/springdoc-plus-ui/index.html")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        final boolean[] passed = {false };
        StepVerifier.create(filter.filter(exchange, chain -> {
            passed[0] = true;
            return reactor.core.publisher.Mono.empty();
        }))
                .verifyComplete();

        assertThat(passed[0]).isTrue();
    }

    /**
     * 测试 /springdoc-plus-gateway/** 路径受保护
     */
    @Test
    void testGatewayPath_protected() {
        String encoded = Base64.getEncoder().encodeToString("admin:123456".getBytes());
        MockServerHttpRequest request = MockServerHttpRequest.get("/springdoc-plus-gateway/openapi/groups")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        final boolean[] passed = {false };
        StepVerifier.create(filter.filter(exchange, chain -> {
            passed[0] = true;
            return reactor.core.publisher.Mono.empty();
        }))
                .verifyComplete();

        assertThat(passed[0]).isTrue();
    }
}
