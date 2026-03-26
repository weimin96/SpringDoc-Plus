package io.github.weimin96.springdocplus.gateway.controller;

import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import io.github.weimin96.springdocplus.gateway.discover.DiscoverGroupsService;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * 网关 OpenAPI 控制器。
 * <p>
 * 提供网关聚合文档的分组列表接口，供前端 UI 获取可用的服务分组。
 *
 * @author pwm
 */
@RestController
public class SpringdocPlusGatewayOpenApiController {

    private final SpringdocPlusGatewayProperties props;
    private final DiscoverGroupsService discoverGroupsService;

    private final DiscoveryClient discoveryClient;

    /**
     * 构造器
     *
     * @param props                  网关配置属性
     * @param discoverGroupsService 分组服务
     * @param discoveryClientProvider 服务发现客户端提供者
     */
    public SpringdocPlusGatewayOpenApiController(
            SpringdocPlusGatewayProperties props,
            DiscoverGroupsService discoverGroupsService,
            org.springframework.beans.factory.ObjectProvider<DiscoveryClient> discoveryClientProvider
    ) {
        this.props = props;
        this.discoverGroupsService = discoverGroupsService;
        this.discoveryClient = discoveryClientProvider.getIfAvailable();
    }

    /**
     * UI 获取分组列表。
     *
     * @return 分组响应，包含网关聚合的路由列表
     */
    @GetMapping(value = "/springdoc-plus-gateway/openapi/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public GroupsResponse groups() {
        Optional<List<String>> serviceIds = Optional.empty();
        if (props.getStrategy() == GatewayStrategy.DISCOVER && props.getDiscover().isEnabled() && discoveryClient != null) {
            serviceIds = Optional.of(discoveryClient.getServices());
        }
        List<GatewayRoute> groups = discoverGroupsService.getGroups(serviceIds);
        return new GroupsResponse(groups);
    }

    /**
     * 分组响应记录
     *
     * @param groups 网关路由列表
     */
    public record GroupsResponse(List<GatewayRoute> groups) {
    }
}
