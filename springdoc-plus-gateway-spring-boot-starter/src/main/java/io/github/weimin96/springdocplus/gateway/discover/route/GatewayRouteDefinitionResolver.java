package io.github.weimin96.springdocplus.gateway.discover.route;

import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 从 Spring Cloud Gateway 的 RouteDefinition 中解析服务路由。
 *
 * @author pwm
 */
public class GatewayRouteDefinitionResolver {

    private final RouteDefinitionLocator locator;
    private final List<GatewayRoutePathResolver> pathResolvers;

    /**
     * 构造器
     *
     * @param locator 网关路由定义定位器
     */
    public GatewayRouteDefinitionResolver(RouteDefinitionLocator locator) {
        this(locator, List.of(
                new SetPathResolver(),
                new RewritePathResolver(),
                new PrefixPathResolver(),
                new PathPredicateResolver()
        ));
    }

    GatewayRouteDefinitionResolver(RouteDefinitionLocator locator, List<GatewayRoutePathResolver> pathResolvers) {
        this.locator = locator;
        this.pathResolvers = pathResolvers;
    }

    /**
     * 解析网关路由定义为可用的路由信息。
     *
     * @return 解析后的路由信息流
     */
    public Flux<ResolvedRoute> resolve() {
        return locator.getRouteDefinitions()
                .handle((routeDefinition, sink) -> {
                    ResolvedRoute resolved = toResolved(routeDefinition);
                    if (resolved != null) {
                        sink.next(resolved);
                    }
                });
    }

    private ResolvedRoute toResolved(RouteDefinition routeDefinition) {
        URI uri = routeDefinition.getUri();
        if (uri == null || !"lb".equalsIgnoreCase(uri.getScheme())) {
            return null;
        }
        String serviceId = uri.getHost();
        if (serviceId == null || serviceId.isBlank()) {
            return null;
        }

        String rawPath = extractFirstPathPattern(routeDefinition).orElse(null);
        Integer stripPrefix = extractStripPrefix(routeDefinition).orElse(null);
        String contextPath = resolveContextPath(routeDefinition).orElse(null);
        return new ResolvedRoute(serviceId, contextPath, rawPath, stripPrefix);
    }

    private Optional<String> resolveContextPath(RouteDefinition routeDefinition) {
        for (GatewayRoutePathResolver resolver : pathResolvers) {
            Optional<String> contextPath = resolver.resolve(routeDefinition);
            if (contextPath.isPresent()) {
                return contextPath;
            }
        }
        return Optional.empty();
    }

    static Optional<String> extractFirstPathPattern(RouteDefinition routeDefinition) {
        return routeDefinition.getPredicates().stream()
                .filter(predicate -> "Path".equalsIgnoreCase(predicate.getName()))
                .findFirst()
                .flatMap(GatewayRouteDefinitionResolver::firstGeneratedArg);
    }

    static Optional<Integer> extractStripPrefix(RouteDefinition routeDefinition) {
        return routeDefinition.getFilters().stream()
                .filter(filter -> "StripPrefix".equalsIgnoreCase(filter.getName()))
                .findFirst()
                .flatMap(filter -> firstGeneratedArg(filter).flatMap(GatewayRouteDefinitionResolver::parseInteger));
    }

    static Optional<String> extractFilterArg(RouteDefinition routeDefinition, String filterName, int index) {
        String key = NameUtils.GENERATED_NAME_PREFIX + index;
        return routeDefinition.getFilters().stream()
                .filter(filter -> filterName.equalsIgnoreCase(filter.getName()))
                .findFirst()
                .map(FilterDefinition::getArgs)
                .map(args -> args.get(key))
                .filter(value -> value != null && !value.isBlank());
    }

    private static Optional<String> firstGeneratedArg(PredicateDefinition predicate) {
        return firstGeneratedArg(predicate.getArgs());
    }

    private static Optional<String> firstGeneratedArg(FilterDefinition filter) {
        return firstGeneratedArg(filter.getArgs());
    }

    private static Optional<String> firstGeneratedArg(Map<String, String> args) {
        return Optional.ofNullable(args.get(NameUtils.GENERATED_NAME_PREFIX + "0"))
                .filter(value -> !value.isBlank());
    }

    private static Optional<Integer> parseInteger(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    static Optional<String> normalizeFirstPatternContextPath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return Optional.empty();
        }
        String path = rawPath.trim();
        int comma = path.indexOf(',');
        if (comma >= 0) {
            path = path.substring(0, comma);
        }
        path = path.replace("**", "");
        return firstPathSegment(path);
    }

    static Optional<String> firstPathSegment(String path) {
        if (path == null || path.isBlank()) {
            return Optional.empty();
        }
        String normalized = path.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        String[] segments = normalized.split("/");
        if (segments.length < 2 || segments[1].isBlank()) {
            return Optional.of("/");
        }
        return Optional.of("/" + segments[1]);
    }

    private static final class PathPredicateResolver implements GatewayRoutePathResolver {

        @Override
        public Optional<String> resolve(RouteDefinition routeDefinition) {
            return extractFirstPathPattern(routeDefinition)
                    .flatMap(GatewayRouteDefinitionResolver::normalizeFirstPatternContextPath);
        }
    }

    private static final class PrefixPathResolver implements GatewayRoutePathResolver {

        @Override
        public Optional<String> resolve(RouteDefinition routeDefinition) {
            Optional<String> prefix = extractFilterArg(routeDefinition, "PrefixPath", 0);
            Optional<String> path = extractFirstPathPattern(routeDefinition);
            if (prefix.isEmpty() || path.isEmpty()) {
                return Optional.empty();
            }
            if (isCatchAllPath(path.get())) {
                return firstPathSegment(prefix.get());
            }
            return Optional.empty();
        }

        private boolean isCatchAllPath(String path) {
            String value = path.trim();
            return "/**".equals(value) || "/".equals(value);
        }
    }

    private static final class RewritePathResolver implements GatewayRoutePathResolver {

        @Override
        public Optional<String> resolve(RouteDefinition routeDefinition) {
            Optional<String> regexp = extractFilterArg(routeDefinition, "RewritePath", 0);
            Optional<String> path = extractFirstPathPattern(routeDefinition);
            if (regexp.isEmpty() || path.isEmpty()) {
                return Optional.empty();
            }
            Optional<String> regexPrefix = staticRegexPrefix(regexp.get());
            Optional<String> pathPrefix = normalizeFirstPatternContextPath(path.get());
            if (regexPrefix.isPresent() && regexPrefix.equals(pathPrefix)) {
                return regexPrefix;
            }
            return Optional.empty();
        }

        private Optional<String> staticRegexPrefix(String regexp) {
            String value = regexp.trim();
            int groupIndex = value.indexOf("(?<");
            int wildcardIndex = value.indexOf(".*");
            int end = firstPositive(groupIndex, wildcardIndex, value.length());
            value = value.substring(0, end);
            value = value.replace("\\/", "/");
            value = value.replace("/?", "");
            return firstPathSegment(value);
        }

        private int firstPositive(int left, int right, int fallback) {
            int result = fallback;
            if (left >= 0) {
                result = Math.min(result, left);
            }
            if (right >= 0) {
                result = Math.min(result, right);
            }
            return result;
        }
    }

    private static final class SetPathResolver implements GatewayRoutePathResolver {

        @Override
        public Optional<String> resolve(RouteDefinition routeDefinition) {
            Optional<String> template = extractFilterArg(routeDefinition, "SetPath", 0);
            Optional<String> path = extractFirstPathPattern(routeDefinition);
            if (template.isEmpty() || path.isEmpty()) {
                return Optional.empty();
            }
            Optional<String> pathPrefix = normalizeFirstPatternContextPath(path.get());
            Optional<String> templatePrefix = firstPathSegment(template.get());
            if (templatePrefix.isPresent() && !templatePrefix.get().contains("{")) {
                return templatePrefix;
            }
            return pathPrefix;
        }
    }

    /**
     * 解析后的路由信息记录。
     *
     * @param serviceId       服务 ID
     * @param contextPath     上下文路径
     * @param rawPathPattern  原始路径模式
     * @param stripPrefix     剥离前缀数量
     */
    public record ResolvedRoute(String serviceId, String contextPath, String rawPathPattern, Integer stripPrefix) {
    }
}
