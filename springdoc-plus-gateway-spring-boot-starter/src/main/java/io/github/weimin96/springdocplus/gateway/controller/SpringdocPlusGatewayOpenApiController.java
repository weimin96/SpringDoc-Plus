package io.github.weimin96.springdocplus.gateway.controller;

import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import io.github.weimin96.springdocplus.gateway.discover.DiscoverGroupsService;
import io.github.weimin96.springdocplus.gateway.proxy.OpenApiSpecProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Optional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 网关 OpenAPI 控制器。
 * <p>
 * 提供网关聚合文档的分组列表接口，供前端 UI 获取可用的服务分组。
 *
 * @author pwm
 */
@RestController
public class SpringdocPlusGatewayOpenApiController {

    private static final Logger log = LoggerFactory.getLogger(SpringdocPlusGatewayOpenApiController.class);

    private final SpringdocPlusGatewayProperties props;
    private final DiscoverGroupsService discoverGroupsService;
    private final OpenApiSpecProxyService openApiSpecProxyService;

    private final DiscoveryClient discoveryClient;
    private final ReactiveDiscoveryClient reactiveDiscoveryClient;

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
            OpenApiSpecProxyService openApiSpecProxyService,
            org.springframework.beans.factory.ObjectProvider<DiscoveryClient> discoveryClientProvider,
            org.springframework.beans.factory.ObjectProvider<ReactiveDiscoveryClient> reactiveDiscoveryClientProvider
    ) {
        this.props = props;
        this.discoverGroupsService = discoverGroupsService;
        this.openApiSpecProxyService = openApiSpecProxyService;
        this.discoveryClient = discoveryClientProvider.getIfAvailable();
        this.reactiveDiscoveryClient = reactiveDiscoveryClientProvider.getIfAvailable();
    }

    /**
     * UI 获取分组列表。
     *
     * @return 分组响应，包含网关聚合的路由列表
     */
    @GetMapping(value = "/springdoc-plus-gateway/openapi/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GroupsResponse> groups() {
        return resolveServiceIds()
                .flatMap(discoverGroupsService::getGroupsReactive)
                .map(groups -> {
                    log.debug("返回聚合文档分组 {} 个", groups.size());
                    return new GroupsResponse(groups);
                });
    }

    /**
     * 代理获取指定服务分组的 OpenAPI 文档。
     *
     * @param service  服务 ID
     * @param group    分组名称
     * @param exchange 当前请求上下文
     * @return OpenAPI 文档响应
     */
    @GetMapping(value = "/springdoc-plus-gateway/openapi/spec")
    public Mono<ResponseEntity<byte[]>> spec(
            @RequestParam("service") String service,
            @RequestParam(value = "group", defaultValue = "default") String group,
            ServerWebExchange exchange) {
        return resolveServiceIds()
                .flatMap(discoverGroupsService::getGroupsReactive)
                .flatMap(routes -> openApiSpecProxyService.proxy(exchange, routes, service, group));
    }

    private Mono<Optional<List<String>>> resolveServiceIds() {
        if (props.getStrategy() != GatewayStrategy.DISCOVER || !props.getDiscover().isEnabled()) {
            return Mono.just(Optional.empty());
        }
        if (reactiveDiscoveryClient != null) {
            return reactiveDiscoveryClient.getServices()
                    .collectList()
                    .doOnNext(services -> log.debug("ReactiveDiscoveryClient 获取到服务列表: {}", services))
                    .map(Optional::of);
        }
        if (discoveryClient != null) {
            return Mono.fromCallable(discoveryClient::getServices)
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnNext(services -> log.debug("DiscoveryClient 获取到服务列表: {}", services))
                    .map(Optional::of);
        }
        log.debug("discover 模式已启用，但当前没有可用的 DiscoveryClient");
        return Mono.just(Optional.empty());
    }

    /**
     * 分组响应记录
     *
     * @param groups 网关路由列表
     */
    public record GroupsResponse(List<GatewayRoute> groups) {
    }
}
