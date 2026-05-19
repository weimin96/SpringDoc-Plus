package io.github.weimin96.springdocplus.gateway.security;

import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 文档 UI 响应安全头。
 *
 * @author pwm
 */
public class SpringdocPlusSecurityHeadersWebFilter implements WebFilter {

    private static final String CONTENT_SECURITY_POLICY = String.join("; ",
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' 'unsafe-inline'",
            "img-src 'self' data:",
            "font-src 'self' data:",
            "connect-src 'self' http: https:",
            "object-src 'none'",
            "base-uri 'self'",
            "frame-ancestors 'self'");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (shouldApply(exchange)) {
            exchange.getResponse().beforeCommit(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                addIfAbsent(headers, "Content-Security-Policy", CONTENT_SECURITY_POLICY);
                addIfAbsent(headers, "X-Content-Type-Options", "nosniff");
                addIfAbsent(headers, "Referrer-Policy", "strict-origin-when-cross-origin");
                addIfAbsent(headers, "X-Frame-Options", "SAMEORIGIN");
                if (isIndex(exchange)) {
                    headers.set(HttpHeaders.CACHE_CONTROL, "no-cache");
                }
                return Mono.empty();
            });
        }
        return chain.filter(exchange);
    }

    private boolean shouldApply(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        return "/doc.html".equals(path) || path.startsWith("/springdoc-plus-ui/");
    }

    private boolean isIndex(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        return "/doc.html".equals(path) || "/springdoc-plus-ui/index.html".equals(path);
    }

    private void addIfAbsent(HttpHeaders headers, String name, String value) {
        if (headers.getFirst(name) == null) {
            headers.add(name, value);
        }
    }
}
