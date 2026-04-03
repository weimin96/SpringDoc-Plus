package io.github.weimin96.springdocplus.gateway.discover;

import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DiscoverGroupsService 单元测试
 *
 * @author pwm
 */
class DiscoverGroupsServiceTest {

    private SpringdocPlusGatewayProperties props;
    private RouteDefinitionLocator routeDefinitionLocator;

    @BeforeEach
    void setUp() {
        props = new SpringdocPlusGatewayProperties();
        routeDefinitionLocator = () -> reactor.core.publisher.Flux.empty();
    }

    /**
     * 测试 MANUAL 模式：直接返回手动配置的路由
     */
    @Test
    void testManualStrategy_returnsConfiguredRoutes() {
        props.setStrategy(GatewayStrategy.MANUAL);

        GatewayRoute route1 = new GatewayRoute();
        route1.setName("用户服务");
        route1.setServiceName("user-service");
        route1.setContextPath("/user-service");
        route1.setUrl("/user-service/v3/api-docs");
        route1.setOrder(1);

        GatewayRoute route2 = new GatewayRoute();
        route2.setName("订单服务");
        route2.setServiceName("order-service");
        route2.setContextPath("/order-service");
        route2.setUrl("/order-service/v3/api-docs");
        route2.setOrder(2);

        props.setRoutes(Arrays.asList(route1, route2));

        DiscoverGroupsService service = new DiscoverGroupsService(props, routeDefinitionLocator);

        List<GatewayRoute> result = service.getGroups(Optional.empty());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("用户服务");
        assertThat(result.get(1).getName()).isEqualTo("订单服务");
    }

    /**
     * 测试 DISCOVER 模式：基于服务发现生成路由
     */
    @Test
    void testDiscoverStrategy_generatesRoutesFromDiscovery() {
        props.setStrategy(GatewayStrategy.DISCOVER);
        props.getDiscover().setEnabled(true);
        props.getDiscover().setResolveContextPathFromGatewayRoutes(false);

        DiscoverGroupsService service = new DiscoverGroupsService(props, routeDefinitionLocator);

        List<String> serviceIds = Arrays.asList("user-service", "order-service");
        List<GatewayRoute> result = service.getGroups(Optional.of(serviceIds));

        assertThat(result).hasSize(2);
        // 验证 contextPath 默认为 / + serviceId
        assertThat(result).anyMatch(r -> r.getServiceName().equals("user-service")
                && r.getContextPath().equals("/user-service"));
        assertThat(result).anyMatch(r -> r.getServiceName().equals("order-service")
                && r.getContextPath().equals("/order-service"));
    }

    /**
     * 测试 DISCOVER 模式：排除指定服务
     */
    @Test
    void testDiscoverStrategy_excludesServices() {
        props.setStrategy(GatewayStrategy.DISCOVER);
        props.getDiscover().setEnabled(true);
        props.getDiscover().setResolveContextPathFromGatewayRoutes(false);
        props.getDiscover().setExcludedServices(Set.of("order-service"));

        DiscoverGroupsService service = new DiscoverGroupsService(props, routeDefinitionLocator);

        List<String> serviceIds = Arrays.asList("user-service", "order-service", "product-service");
        List<GatewayRoute> result = service.getGroups(Optional.of(serviceIds));

        assertThat(result).hasSize(2);
        assertThat(result).noneMatch(r -> r.getServiceName().equals("order-service"));
    }

    /**
     * 测试 DISCOVER 模式：正则表达式排除
     */
    @Test
    void testDiscoverStrategy_excludesServicesRegex() {
        props.setStrategy(GatewayStrategy.DISCOVER);
        props.getDiscover().setEnabled(true);
        props.getDiscover().setResolveContextPathFromGatewayRoutes(false);
        // 排除所有以 -test 结尾的服务
        props.getDiscover().setExcludedServices(Set.of(".*-test"));

        DiscoverGroupsService service = new DiscoverGroupsService(props, routeDefinitionLocator);

        List<String> serviceIds = Arrays.asList("user-service", "order-test", "product-service");
        List<GatewayRoute> result = service.getGroups(Optional.of(serviceIds));

        assertThat(result).hasSize(2);
        assertThat(result).noneMatch(r -> r.getServiceName().equals("order-test"));
    }

    /**
     * 测试 DISCOVER 模式：ServiceConfig 个性化配置
     */
    @Test
    void testDiscoverStrategy_serviceConfigOverride() {
        props.setStrategy(GatewayStrategy.DISCOVER);
        props.getDiscover().setEnabled(true);
        props.getDiscover().setResolveContextPathFromGatewayRoutes(false);

        // 配置 user-service 的个性化配置
        SpringdocPlusGatewayProperties.ServiceConfig sc = new SpringdocPlusGatewayProperties.ServiceConfig();
        sc.setContextPath("/api/users");
        sc.setGroupName("用户中心");
        sc.setGroupNames(Arrays.asList("admin", "normal"));
        sc.setOrder(10);
        props.getDiscover().getServiceConfig().put("user-service", sc);

        DiscoverGroupsService service = new DiscoverGroupsService(props, routeDefinitionLocator);

        List<String> serviceIds = Collections.singletonList("user-service");
        List<GatewayRoute> result = service.getGroups(Optional.of(serviceIds));

        // 应生成 3 个分组：default + admin + normal
        assertThat(result).hasSize(3);

        // default 分组
        GatewayRoute defaultRoute = result.stream()
                .filter(r -> r.getGroup() == null)
                .findFirst().orElse(null);
        assertThat(defaultRoute).isNotNull();
        assertThat(defaultRoute.getContextPath()).isEqualTo("/api/users");
        assertThat(defaultRoute.getName()).isEqualTo("用户中心");
        assertThat(defaultRoute.getOrder()).isEqualTo(10);

        // admin 分组
        GatewayRoute adminRoute = result.stream()
                .filter(r -> "admin".equals(r.getGroup()))
                .findFirst().orElse(null);
        assertThat(adminRoute).isNotNull();
        assertThat(adminRoute.getUrl()).contains("group=admin");

        // normal 分组
        GatewayRoute normalRoute = result.stream()
                .filter(r -> "normal".equals(r.getGroup()))
                .findFirst().orElse(null);
        assertThat(normalRoute).isNotNull();
        assertThat(normalRoute.getUrl()).contains("group=normal");
    }

    /**
     * 测试排序：按 order 字段升序
     */
    @Test
    void testSort_byOrderField() {
        props.setStrategy(GatewayStrategy.MANUAL);

        GatewayRoute route1 = new GatewayRoute();
        route1.setName("第三");
        route1.setOrder(3);

        GatewayRoute route2 = new GatewayRoute();
        route2.setName("第一");
        route2.setOrder(1);

        GatewayRoute route3 = new GatewayRoute();
        route3.setName("第二");
        route3.setOrder(2);

        props.setRoutes(Arrays.asList(route1, route2, route3));

        DiscoverGroupsService service = new DiscoverGroupsService(props, routeDefinitionLocator);

        List<GatewayRoute> result = service.getGroups(Optional.empty());

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("第一");
        assertThat(result.get(1).getName()).isEqualTo("第二");
        assertThat(result.get(2).getName()).isEqualTo("第三");
    }

    /**
     * 测试空服务列表
     */
    @Test
    void testEmptyServiceIds() {
        props.setStrategy(GatewayStrategy.DISCOVER);
        props.getDiscover().setEnabled(true);

        DiscoverGroupsService service = new DiscoverGroupsService(props, routeDefinitionLocator);

        List<GatewayRoute> result = service.getGroups(Optional.of(Collections.emptyList()));

        assertThat(result).isEmpty();
    }

    /**
     * 测试 Optional.empty()（无服务发现）
     */
    @Test
    void testNoServiceIds() {
        props.setStrategy(GatewayStrategy.DISCOVER);
        props.getDiscover().setEnabled(true);

        DiscoverGroupsService service = new DiscoverGroupsService(props, routeDefinitionLocator);

        List<GatewayRoute> result = service.getGroups(Optional.empty());

        assertThat(result).isEmpty();
    }

    /**
     * 测试 null routes 保护
     */
    @Test
    void testNullRoutes_protection() {
        props.setStrategy(GatewayStrategy.MANUAL);
        props.setRoutes(null);

        DiscoverGroupsService service = new DiscoverGroupsService(props, routeDefinitionLocator);

        List<GatewayRoute> result = service.getGroups(Optional.empty());

        assertThat(result).isEmpty();
    }
}