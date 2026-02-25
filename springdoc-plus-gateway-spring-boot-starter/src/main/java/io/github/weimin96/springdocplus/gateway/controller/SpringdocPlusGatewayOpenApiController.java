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
 * @author pwm
 */
@RestController
public class SpringdocPlusGatewayOpenApiController {

    private final SpringdocPlusGatewayProperties props;
    private final DiscoverGroupsService discoverGroupsService;

    private final DiscoveryClient discoveryClient;

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

    public record GroupsResponse(List<GatewayRoute> groups) {
    }
}
