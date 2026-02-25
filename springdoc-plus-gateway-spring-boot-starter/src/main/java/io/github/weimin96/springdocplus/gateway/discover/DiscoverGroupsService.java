package io.github.weimin96.springdocplus.gateway.discover;

import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import io.github.weimin96.springdocplus.gateway.discover.route.GatewayRouteDefinitionResolver;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 生成“可用分组列表”。
 * <p>
 * - manual: 直接使用 springdoc-plus.gateway.routes
 * - discover:
 * - 以 DiscoveryClient 的 serviceId 列表为准（感知上下线）
 * - 可选：结合 Gateway RouteDefinition 推断 contextPath（更接近 Knife4j 4.5 行为）
 * - 支持 service-config 中的 group-names 生成多分组入口
 *
 * @author pwm
 */
public class DiscoverGroupsService {

    private final SpringdocPlusGatewayProperties props;
    private final RouteDefinitionLocator routeDefinitionLocator;

    public DiscoverGroupsService(SpringdocPlusGatewayProperties props, RouteDefinitionLocator routeDefinitionLocator) {
        this.props = props;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    public List<GatewayRoute> getGroups(Optional<List<String>> discoverServiceIds) {
        if (props.getStrategy() == GatewayStrategy.MANUAL) {
            return sort(copy(props.getRoutes()));
        }

        // DISCOVER
        List<GatewayRoute> routes = new ArrayList<>();

        // 0) 基于 Gateway routes 推断 serviceId -> contextPath
        Map<String, String> inferredContextPath = new HashMap<>();
        if (props.getDiscover().isResolveContextPathFromGatewayRoutes() && routeDefinitionLocator != null) {
            GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(routeDefinitionLocator);
            resolver.resolve().toStream().forEach(r -> {
                if (r.contextPath() != null && !r.contextPath().isBlank()) {
                    inferredContextPath.put(r.serviceId(), r.contextPath());
                }
            });
        }

        // 1) 来自 discover 的默认分组（default）
        discoverServiceIds.ifPresent(serviceIds -> {
            for (String serviceId : serviceIds) {
                if (excluded(serviceId)) {
                    continue;
                }

                SpringdocPlusGatewayProperties.ServiceConfig sc = props.getDiscover().getServiceConfig().get(serviceId);
                String contextPath = sc != null && sc.getContextPath() != null ? sc.getContextPath() : inferredContextPath.get(serviceId);
                if (contextPath == null || contextPath.isBlank()) {
                    // fallback：按常见 pattern 兜底
                    contextPath = "/" + serviceId;
                }

                // default group
                routes.add(buildRoute(serviceId, sc, contextPath, null));

                // extra groups
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

        // 2) routes 中的自定义补充/覆写（支持 manual 混搭）
        for (GatewayRoute custom : props.getRoutes()) {
            routes.removeIf(r -> Objects.equals(r.getServiceName(), custom.getServiceName())
                    && Objects.equals(nullToEmpty(r.getGroup()), nullToEmpty(custom.getGroup())));
            routes.add(custom);
        }

        return sort(routes);
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

        String url = contextPath + props.getDiscover().getOpenapi3Url();
        if (group != null && !group.isBlank()) {
            // 对齐 Knife4j 文档：discover 模式默认聚合 default，其他分组需要显式 group 参数
            if (url.contains("?")) {
                url = url + "&group=" + group;
            } else {
                url = url + "?group=" + group;
            }
        }
        r.setUrl(url);

        r.setOrder(sc != null && sc.getOrder() != null ? sc.getOrder() : 0);
        return r;
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
            } catch (Exception ignore) {
                // ignore invalid regex
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
