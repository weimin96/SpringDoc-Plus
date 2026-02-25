package io.github.weimin96.springdocplus.gateway.security;

import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 对齐 Knife4j Gateway：可选的 HTTP Basic 保护。
 * <p>
 * MVP 实现：仅保护 /doc.html、/springdoc-plus-ui/**、/springdoc-plus-gateway/**
 *
 * @author pwm
 */
public class BasicAuthWebFilter implements WebFilter {

    private final SpringdocPlusGatewayProperties props;

    public BasicAuthWebFilter(SpringdocPlusGatewayProperties props) {
        this.props = props;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        if (props.getBasic() == null || !props.getBasic().isEnabled()) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (!needProtect(path)) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(auth) || !auth.startsWith("Basic ")) {
            return unauthorized(exchange);
        }

        String token = auth.substring("Basic ".length());
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return unauthorized(exchange);
        }

        String expected = (props.getBasic().getUsername() == null ? "" : props.getBasic().getUsername())
                + ":" + (props.getBasic().getPassword() == null ? "" : props.getBasic().getPassword());

        if (!expected.equals(decoded)) {
            return unauthorized(exchange);
        }

        return chain.filter(exchange);
    }

    private boolean needProtect(String path) {
        return "/doc.html".equals(path)
                || path.startsWith("/springdoc-plus-ui")
                || path.startsWith("/springdoc-plus-gateway");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"springdoc-plus\"");
        return exchange.getResponse().setComplete();
    }
}
