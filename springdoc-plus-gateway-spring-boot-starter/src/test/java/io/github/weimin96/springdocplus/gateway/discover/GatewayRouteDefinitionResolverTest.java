package io.github.weimin96.springdocplus.gateway.discover;

import io.github.weimin96.springdocplus.gateway.discover.route.GatewayRouteDefinitionResolver;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GatewayRouteDefinitionResolver 单元测试
 *
 * @author pwm
 */
class GatewayRouteDefinitionResolverTest {

    /**
     * 测试正常解析 lb:// 服务
     */
    @Test
    void testResolve_lbScheme() {
        RouteDefinition rd = new RouteDefinition();
        rd.setId("user-route");
        rd.setUri(URI.create("lb://user-service"));

        Map<String, String> predicates = new HashMap<>();
        predicates.put(NameUtils.GENERATED_NAME_PREFIX + "0", "/user-service/**");
        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName("Path");
        pathPredicate.setArgs(predicates);
        rd.setPredicates(Collections.singletonList(pathPredicate));

        RouteDefinitionLocator locator = () -> Flux.just(rd);
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .assertNext(route -> {
                    assertThat(route.serviceId()).isEqualTo("user-service");
                    assertThat(route.contextPath()).isEqualTo("/user-service");
                    assertThat(route.rawPathPattern()).isEqualTo("/user-service/**");
                })
                .verifyComplete();
    }

    /**
     * 测试非 lb:// 方案被过滤
     */
    @Test
    void testResolve_nonLbScheme_filtered() {
        RouteDefinition rd = new RouteDefinition();
        rd.setId("http-route");
        rd.setUri(URI.create("http://example.com"));

        RouteDefinitionLocator locator = () -> Flux.just(rd);
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .verifyComplete();
    }

    /**
     * 测试带 StripPrefix 过滤器的路由
     */
    @Test
    void testResolve_withStripPrefix() {
        RouteDefinition rd = new RouteDefinition();
        rd.setId("api-route");
        rd.setUri(URI.create("lb://api-service"));

        // Path predicate
        Map<String, String> predicates = new HashMap<>();
        predicates.put(NameUtils.GENERATED_NAME_PREFIX + "0", "/api/v1/**");
        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName("Path");
        pathPredicate.setArgs(predicates);
        rd.setPredicates(Collections.singletonList(pathPredicate));

        Map<String, String> filterArgs = new HashMap<>();
        filterArgs.put(NameUtils.GENERATED_NAME_PREFIX + "0", "1");
        FilterDefinition stripPrefixFilter = new FilterDefinition();
        stripPrefixFilter.setName("StripPrefix");
        stripPrefixFilter.setArgs(filterArgs);
        rd.setFilters(Collections.singletonList(stripPrefixFilter));

        RouteDefinitionLocator locator = () -> Flux.just(rd);
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .assertNext(route -> {
                    assertThat(route.serviceId()).isEqualTo("api-service");
                    assertThat(route.contextPath()).isEqualTo("/api");
                    assertThat(route.stripPrefix()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void resolvePrefixPathForCatchAllRoute() {
        RouteDefinition rd = new RouteDefinition();
        rd.setId("prefix-route");
        rd.setUri(URI.create("lb://internal-service"));

        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName("Path");
        pathPredicate.setArgs(Map.of(NameUtils.GENERATED_NAME_PREFIX + "0", "/**"));
        rd.setPredicates(Collections.singletonList(pathPredicate));

        FilterDefinition prefixPathFilter = new FilterDefinition();
        prefixPathFilter.setName("PrefixPath");
        prefixPathFilter.setArgs(Map.of(NameUtils.GENERATED_NAME_PREFIX + "0", "/user-service"));
        rd.setFilters(Collections.singletonList(prefixPathFilter));

        RouteDefinitionLocator locator = () -> Flux.just(rd);
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .assertNext(route -> {
                    assertThat(route.serviceId()).isEqualTo("internal-service");
                    assertThat(route.contextPath()).isEqualTo("/user-service");
                })
                .verifyComplete();
    }

    @Test
    void resolveRewritePathWithMatchingStaticPrefix() {
        RouteDefinition rd = new RouteDefinition();
        rd.setId("rewrite-route");
        rd.setUri(URI.create("lb://order-service"));

        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName("Path");
        pathPredicate.setArgs(Map.of(NameUtils.GENERATED_NAME_PREFIX + "0", "/openapi/v1/order/**"));
        rd.setPredicates(Collections.singletonList(pathPredicate));

        FilterDefinition rewritePathFilter = new FilterDefinition();
        rewritePathFilter.setName("RewritePath");
        rewritePathFilter.setArgs(Map.of(
                NameUtils.GENERATED_NAME_PREFIX + "0", "/openapi/v1/order/(?<segment>.*)",
                NameUtils.GENERATED_NAME_PREFIX + "1", "/${segment}"
        ));
        rd.setFilters(Collections.singletonList(rewritePathFilter));

        RouteDefinitionLocator locator = () -> Flux.just(rd);
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .assertNext(route -> {
                    assertThat(route.serviceId()).isEqualTo("order-service");
                    assertThat(route.contextPath()).isEqualTo("/openapi");
                })
                .verifyComplete();
    }

    @Test
    void resolveSetPathFallsBackToExternalPathWhenTemplateIsDynamic() {
        RouteDefinition rd = new RouteDefinition();
        rd.setId("set-path-route");
        rd.setUri(URI.create("lb://user-service"));

        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName("Path");
        pathPredicate.setArgs(Map.of(NameUtils.GENERATED_NAME_PREFIX + "0", "/internal/doc/user/{segment}"));
        rd.setPredicates(Collections.singletonList(pathPredicate));

        FilterDefinition setPathFilter = new FilterDefinition();
        setPathFilter.setName("SetPath");
        setPathFilter.setArgs(Map.of(NameUtils.GENERATED_NAME_PREFIX + "0", "/{segment}"));
        rd.setFilters(Collections.singletonList(setPathFilter));

        RouteDefinitionLocator locator = () -> Flux.just(rd);
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .assertNext(route -> {
                    assertThat(route.serviceId()).isEqualTo("user-service");
                    assertThat(route.contextPath()).isEqualTo("/internal");
                })
                .verifyComplete();
    }

    /**
     * 测试多路径模式（逗号分隔）取第一个
     */
    @Test
    void testResolve_multiplePathPatterns() {
        RouteDefinition rd = new RouteDefinition();
        rd.setId("multi-route");
        rd.setUri(URI.create("lb://multi-service"));

        // 多个路径用逗号分隔
        Map<String, String> predicates = new HashMap<>();
        predicates.put(NameUtils.GENERATED_NAME_PREFIX + "0", "/service-a/**,/service-b/**");
        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName("Path");
        pathPredicate.setArgs(predicates);
        rd.setPredicates(Collections.singletonList(pathPredicate));

        RouteDefinitionLocator locator = () -> Flux.just(rd);
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .assertNext(route -> {
                    assertThat(route.serviceId()).isEqualTo("multi-service");
                    assertThat(route.contextPath()).isEqualTo("/service-a");
                })
                .verifyComplete();
    }

    /**
     * 测试空路由列表
     */
    @Test
    void testResolve_emptyRoutes() {
        RouteDefinitionLocator locator = () -> Flux.empty();
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .verifyComplete();
    }

    @Test
    void testResolve_nullUriAndBlankHostAreFiltered() {
        RouteDefinition nullUri = new RouteDefinition();
        nullUri.setId("null-uri");

        RouteDefinition blankHost = new RouteDefinition();
        blankHost.setId("blank-host");
        blankHost.setUri(URI.create("lb:///missing-host"));

        RouteDefinitionLocator locator = () -> Flux.just(nullUri, blankHost);
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .verifyComplete();
    }

    @Test
    void testResolve_withoutPathAndInvalidStripPrefix() {
        RouteDefinition rd = new RouteDefinition();
        rd.setId("no-path");
        rd.setUri(URI.create("lb://users"));

        FilterDefinition stripPrefixFilter = new FilterDefinition();
        stripPrefixFilter.setName("StripPrefix");
        stripPrefixFilter.setArgs(Map.of(NameUtils.GENERATED_NAME_PREFIX + "0", "abc"));
        rd.setFilters(Collections.singletonList(stripPrefixFilter));

        RouteDefinitionLocator locator = () -> Flux.just(rd);
        GatewayRouteDefinitionResolver resolver = new GatewayRouteDefinitionResolver(locator);

        StepVerifier.create(resolver.resolve())
                .assertNext(route -> {
                    assertThat(route.serviceId()).isEqualTo("users");
                    assertThat(route.contextPath()).isNull();
                    assertThat(route.stripPrefix()).isNull();
                })
                .verifyComplete();
    }
}
