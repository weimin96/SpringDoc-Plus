package io.github.weimin96.springdocplus.gateway.discover;

import io.github.weimin96.springdocplus.gateway.discover.route.GatewayRouteDefinitionResolver;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 生成“可用分组列表”。
 * <p>
 * - manual: 直接使用 springdoc-plus.gateway.routes
 * - discover:
 * - 以 DiscoveryClient 的 serviceId 列表为准（感知上下线）
 * - 可选：结合 Gateway RouteDefinition 推断 contextPath（更接近 Knife4j 4.5 行为）
 * - 支持 service-config 中的 group-names 生成多分组入口
 * <p>
 * 使用 Caffeine 实现分组列表缓存，避免每次请求都调用服务发现
 *
 * @author pwm
 */
public class DiscoverGroupsService {

    private static final Logger log = LoggerFactory.getLogger(DiscoverGroupsService.class);

    private final SpringdocPlusGatewayProperties props;
    private final RouteDefinitionLocator routeDefinitionLocator;

    private final Cache<String, List<GatewayRoute>> groupsCache;

    /**
     * 构造器
     *
     * @param props                  网关配置属性
     * @param routeDefinitionLocator 网关路由定义定位器
     */
    public DiscoverGroupsService(SpringdocPlusGatewayProperties props, RouteDefinitionLocator routeDefinitionLocator) {
        this.props = props;
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.groupsCache = Caffeine.newBuilder()
                .expireAfterWrite(discoverCacheTtl())
                .maximumSize(discoverCacheMaximumSize())
                .build();
    }

    /**
     * 获取可用的网关路由分组列表。
     * <p>
     * - manual: 直接使用 springdoc-plus.gateway.routes
     * - discover: 以 DiscoveryClient 的 serviceId 列表为准（感知上下线），可选结合 Gateway RouteDefinition 推断 contextPath
     *
     * @param discoverServiceIds 服务发现获取的服务 ID 列表（可选）
     * @return 网关路由分组列表
     */
    public List<GatewayRoute> getGroups(Optional<List<String>> discoverServiceIds) {
        if (props.getStrategy() == GatewayStrategy.MANUAL) {
            return sort(copy(props.getRoutes()));
        }

        Map<String, String> inferredContextPath = resolveContextPathsBlocking();
        String cacheKey = buildCacheKey(discoverServiceIds, inferredContextPath);

        List<GatewayRoute> cached = groupsCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取分组列表，key: {}", cacheKey);
            return copy(cached);
        }

        List<GatewayRoute> result = computeGroups(discoverServiceIds, inferredContextPath);
        groupsCache.put(cacheKey, copy(result));
        log.debug("生成分组列表 {} 个条目，已放入缓存", result.size());
        return result;
    }

    public Mono<List<GatewayRoute>> getGroupsReactive(Optional<List<String>> discoverServiceIds) {
        if (props.getStrategy() == GatewayStrategy.MANUAL) {
            return Mono.just(sort(copy(props.getRoutes())));
        }

        return resolveContextPathsReactive()
                .flatMap(inferredContextPath -> {
                    String cacheKey = buildCacheKey(discoverServiceIds, inferredContextPath);
                    List<GatewayRoute> cached = groupsCache.getIfPresent(cacheKey);
                    if (cached != null) {
                        log.debug("从缓存获取分组列表，key: {}", cacheKey);
                        return Mono.just(copy(cached));
                    }
                    List<GatewayRoute> result = computeGroups(discoverServiceIds, inferredContextPath);
                    groupsCache.put(cacheKey, copy(result));
                    log.debug("生成分组列表 {} 个条目，已放入缓存", result.size());
                    return Mono.just(result);
                });
    }

    /**
     * 计算分组列表（核心逻辑）
     */
    private List<GatewayRoute> computeGroups(Optional<List<String>> discoverServiceIds) {
        return computeGroups(discoverServiceIds, resolveContextPathsBlocking());
    }

    private List<GatewayRoute> computeGroups(Optional<List<String>> discoverServiceIds, Map<String, String> inferredContextPath) {
        List<GatewayRoute> routes = new ArrayList<>();

        discoverServiceIds.ifPresent(serviceIds -> {
            log.debug("服务发现获取到的服务列表: {}", serviceIds);
            for (String serviceId : serviceIds) {
                if (excluded(serviceId)) {
                    continue;
                }

                SpringdocPlusGatewayProperties.ServiceConfig sc = props.getDiscover().getServiceConfig().get(serviceId);
                String contextPath = sc != null && sc.getContextPath() != null ? sc.getContextPath() : inferredContextPath.get(serviceId);
                if (contextPath == null || contextPath.isBlank()) {
                    contextPath = "/" + serviceId;
                }

                routes.add(buildRoute(serviceId, sc, contextPath, null));

                if (sc != null && sc.getGroupNames() != null) {
                    for (String g : sc.getGroupNames()) {
                        if (g == null || g.isBlank()) {
                            continue;
                        }
                        routes.add(buildRoute(serviceId, sc, contextPath, g));
                    }
                }
            }
        });

        for (GatewayRoute custom : copy(props.getRoutes())) {
            routes.removeIf(r -> Objects.equals(r.getServiceName(), custom.getServiceName())
                    && Objects.equals(nullToEmpty(r.getGroup()), nullToEmpty(custom.getGroup())));
            routes.add(custom);
        }

        return sort(routes);
    }

    private Map<String, String> resolveContextPathsBlocking() {
        if (!props.getDiscover().isResolveContextPathFromGatewayRoutes() || routeDefinitionLocator == null) {
            return Collections.emptyMap();
        }
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(routeDefinitionLocator);
        try {
            List<GatewayRouteDefinitionResolver.ResolvedRoute> resolvedRoutes = resolver.resolve()
                    .collectList()
                    .block(discoverTimeout());
            return toContextPathMap(resolvedRoutes == null ? Collections.emptyList() : resolvedRoutes);
        } catch (Exception e) {
            log.warn("从 Gateway 路由定义解析 contextPath 失败: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Mono<Map<String, String>> resolveContextPathsReactive() {
        if (!props.getDiscover().isResolveContextPathFromGatewayRoutes() || routeDefinitionLocator == null) {
            return Mono.just(Collections.emptyMap());
        }
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(routeDefinitionLocator);
        return resolver.resolve()
                .collectList()
                .timeout(discoverTimeout())
                .map(this::toContextPathMap)
                .onErrorResume(ex -> {
                    log.warn("从 Gateway 路由定义解析 contextPath 失败: {}", ex.getMessage());
                    return Mono.just(Collections.emptyMap());
                });
    }

    private Map<String, String> toContextPathMap(List<GatewayRouteDefinitionResolver.ResolvedRoute> resolvedRoutes) {
        Map<String, String> inferredContextPath = new HashMap<>();
        for (GatewayRouteDefinitionResolver.ResolvedRoute route : resolvedRoutes) {
            if (route.contextPath() != null && !route.contextPath().isBlank()) {
                inferredContextPath.putIfAbsent(route.serviceId(), route.contextPath());
            }
        }
        log.debug("从 Gateway 路由定义推断 contextPath: {}", inferredContextPath);
        return inferredContextPath;
    }

    private String buildCacheKey(Optional<List<String>> discoverServiceIds, Map<String, String> inferredContextPath) {
        List<String> normalized = new ArrayList<>(discoverServiceIds.orElseGet(Collections::emptyList));
        normalized.sort(String.CASE_INSENSITIVE_ORDER);
        return "groups-" + Objects.hash(
                normalized,
                inferredContextPath,
                props.getDiscover().getOpenapi3Url(),
                props.getDiscover().getExcludedServices(),
                props.getDiscover().getServiceConfig(),
                props.getRoutes());
    }

    private GatewayRoute buildRoute(String serviceId, SpringdocPlusGatewayProperties.ServiceConfig sc, String contextPath, String group) {
        GatewayRoute r = new GatewayRoute();
        r.setServiceName(serviceId);
        r.setContextPath(contextPath);
        r.setGroup(group);

        String display = sc != null && sc.getGroupName() != null ? sc.getGroupName() : serviceId;
        if (group != null && !group.isBlank()) {
            r.setName(display + " - " + group);
        } else {
            r.setName(display);
        }

        r.setUrl(buildOpenApiUrl(contextPath, group));

        r.setOrder(sc != null && sc.getOrder() != null ? sc.getOrder() : 0);
        return r;
    }

    private String buildOpenApiUrl(String contextPath, String group) {
        String openapiUrl = props.getDiscover().getOpenapi3Url();
        String path = openapiUrl;
        String query = null;
        int queryStart = openapiUrl.indexOf('?');
        if (queryStart >= 0) {
            path = openapiUrl.substring(0, queryStart);
            query = openapiUrl.substring(queryStart + 1);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(joinPath(contextPath, path));
        if (query != null && !query.isBlank()) {
            builder.query(query);
        }
        if (group != null && !group.isBlank()) {
            // 对齐 Knife4j 文档：discover 模式默认聚合 default，其他分组需要显式 group 参数。
            builder.queryParam("group", group);
        }
        return builder.build().encode().toUriString();
    }

    private String joinPath(String contextPath, String openapiPath) {
        String left = contextPath == null || contextPath.isBlank() ? "/" : contextPath;
        String right = openapiPath == null || openapiPath.isBlank() ? "/" : openapiPath;
        if (!left.startsWith("/")) {
            left = "/" + left;
        }
        if (left.endsWith("/") && right.startsWith("/")) {
            return left + right.substring(1);
        }
        if (!left.endsWith("/") && !right.startsWith("/")) {
            return left + "/" + right;
        }
        return left + right;
    }

    private Duration discoverCacheTtl() {
        Duration ttl = props.getDiscover().getCache().getTtl();
        return ttl == null || ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(60) : ttl;
    }

    private long discoverCacheMaximumSize() {
        long maximumSize = props.getDiscover().getCache().getMaximumSize();
        return maximumSize <= 0 ? 10 : maximumSize;
    }

    private Duration discoverTimeout() {
        Duration timeout = props.getDiscover().getTimeout();
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(3) : timeout;
    }

    private boolean excluded(String serviceId) {
        if (props.getDiscover().getExcludedServices() == null) {
            return false;
        }
        for (String exp : props.getDiscover().getExcludedServices()) {
            if (exp == null || exp.isBlank()) {
                continue;
            }
            if (exp.equalsIgnoreCase(serviceId)) {
                return true;
            }
            try {
                if (Pattern.compile(exp, Pattern.CASE_INSENSITIVE).matcher(serviceId).matches()) {
                    return true;
                }
            } catch (Exception ex) {
                log.warn("忽略非法 excluded-services 正则 [{}]: {}", exp, ex.getMessage());
            }
        }
        return false;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static List<GatewayRoute> copy(List<GatewayRoute> in) {
        if (in == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(in);
    }

    private static List<GatewayRoute> sort(List<GatewayRoute> routes) {
        routes.sort(Comparator.comparingInt(r -> r.getOrder() == null ? 0 : r.getOrder()));
        return routes;
    }
}
