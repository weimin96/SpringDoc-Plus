package io.github.weimin96.springdocplus.gateway.discover.route;

import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.Optional;

/**
 * Gateway 路由 contextPath 推断策略。
 *
 * @author pwm
 */
public interface GatewayRoutePathResolver {

    /**
     * 从路由定义中推断对外暴露的 contextPath。
     *
     * @param routeDefinition Gateway 路由定义
     * @return 推断出的 contextPath，无法可靠推断时返回空
     */
    Optional<String> resolve(RouteDefinition routeDefinition);
}
