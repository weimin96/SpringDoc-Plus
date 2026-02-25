package io.github.weimin96.springdocplus.gateway.discover.route;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.Objects;

/**
 * 从 Spring Cloud Gateway 的 RouteDefinition 中解析：
 * - serviceId（lb://SERVICE）
 * - contextPath（基于 Path predicate + StripPrefix filter 推断）
 *
 * @author pwm
 */
public class GatewayRouteDefinitionResolver {

    private final RouteDefinitionLocator locator;

    public GatewayRouteDefinitionResolver(RouteDefinitionLocator locator) {
        this.locator = locator;
    }

    public Flux<ResolvedRoute> resolve() {
        return locator.getRouteDefinitions()
                .filter(obj -> true)
                .map(this::toResolved)
                .filter(Objects::nonNull);
    }

    private ResolvedRoute toResolved(RouteDefinition rd) {
        URI uri = rd.getUri();
        if (uri == null) {
            return null;
        }

        // only handle lb://SERVICE
        if (!"lb".equalsIgnoreCase(uri.getScheme())) {
            return null;
        }
        String serviceId = uri.getHost();
        if (serviceId == null || serviceId.isBlank()) {
            return null;
        }

        String rawPath = extractFirstPathPattern(rd);
        Integer stripPrefix = extractStripPrefix(rd);

        String contextPath = null;
        if (rawPath != null) {
            contextPath = normalizeContextPath(rawPath, stripPrefix);
        }

        return new ResolvedRoute(serviceId, contextPath, rawPath, stripPrefix);
    }

    private static String extractFirstPathPattern(RouteDefinition rd) {
        return rd.getPredicates().stream()
                .filter(p -> "Path".equalsIgnoreCase(p.getName()))
                .findFirst()
                .map(p -> p.getArgs().getOrDefault("_genkey_0", null))
                .orElse(null);
    }

    private static Integer extractStripPrefix(RouteDefinition rd) {
        return rd.getFilters().stream()
                .filter(f -> "StripPrefix".equalsIgnoreCase(f.getName()))
                .findFirst()
                .map(f -> {
                    String v = f.getArgs().getOrDefault("_genkey_0", null);
                    try {
                        return Integer.parseInt(v);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    /**
     * rawPath 常见：/user-service/** 或 /user-service/v3/api-docs
     * <p>
     * stripPrefix=1 时，实际转发给下游的 path 会去掉第一段 /user-service。
     * 但在网关层面对外暴露的 contextPath 仍然是 /user-service。
     */
    private static String normalizeContextPath(String rawPath, Integer stripPrefix) {
        String p = rawPath.trim();
        // pick first pattern if multiple split by ,
        int comma = p.indexOf(',');
        if (comma >= 0) {
            p = p.substring(0, comma);
        }

        // remove wildcards
        p = p.replace("**", "");
        // ensure leading /
        if (!p.startsWith("/")) {
            p = "/" + p;
        }

        // keep only first path segment as contextPath
        // e.g. /user-service/ -> /user-service
        String[] seg = p.split("/");
        if (seg.length < 2) {
            return "/";
        }
        return "/" + seg[1];
    }

    public record ResolvedRoute(String serviceId, String contextPath, String rawPathPattern, Integer stripPrefix) {
    }
}
