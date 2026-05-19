package io.github.weimin96.springdocplus.gateway.proxy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.gateway.properties.SpringdocPlusGatewayProperties;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/**
 * OpenAPI 文档代理服务。
 *
 * @author pwm
 */
public class OpenApiSpecProxyService {

    private static final String DEFAULT_GROUP = "default";

    private final SpringdocPlusGatewayProperties props;
    private final WebClient webClient;
    private final Cache<ProxyCacheKey, CachedSpec> specCache;

    /**
     * 构造器
     *
     * @param props            网关配置属性
     * @param webClientBuilder WebClient 构建器
     */
    public OpenApiSpecProxyService(
            SpringdocPlusGatewayProperties props,
            WebClient.Builder webClientBuilder) {
        this(props, webClientBuilder.codecs(codecs ->
                codecs.defaultCodecs().maxInMemorySize(maxDocumentBytes(props))).build());
    }

    OpenApiSpecProxyService(SpringdocPlusGatewayProperties props, WebClient webClient) {
        this.props = props;
        this.webClient = webClient;
        this.specCache = Caffeine.newBuilder()
                .expireAfterWrite(proxyCacheTtl())
                .maximumSize(proxyCacheMaximumSize())
                .build();
    }

    /**
     * 代理获取 OpenAPI 文档。
     *
     * @param exchange 当前请求上下文
     * @param routes   当前可用分组
     * @param service  服务 ID
     * @param group    分组名称
     * @return OpenAPI 文档响应
     */
    public Mono<ResponseEntity<byte[]>> proxy(
            ServerWebExchange exchange,
            List<GatewayRoute> routes,
            String service,
            String group) {
        return Mono.defer(() -> {
            if (!props.getProxy().isEnabled()) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "OpenAPI 文档代理未启用"));
            }

            GatewayRoute route = findRoute(routes, service, normalizeGroup(group));
            URI targetUri = buildTargetUri(exchange, route);
            ProxyCacheKey cacheKey = new ProxyCacheKey(service, normalizeGroup(group), route.getUrl());
            CachedSpec cached = specCache.getIfPresent(cacheKey);
            if (cached != null) {
                return Mono.just(toResponse(exchange, cached));
            }

            return webClient.get()
                    .uri(targetUri)
                    .accept(MediaType.APPLICATION_JSON, MediaType.valueOf("application/yaml"), MediaType.valueOf("application/x-yaml"))
                    .retrieve()
                    .toEntity(byte[].class)
                    .timeout(proxyTimeout())
                    .map(response -> toCachedSpec(response, targetUri))
                    .map(spec -> {
                        specCache.put(cacheKey, spec);
                        return toResponse(exchange, spec);
                    })
                    .onErrorMap(WebClientResponseException.class, ex ->
                            new ResponseStatusException(HttpStatus.BAD_GATEWAY, "下游 OpenAPI 文档返回异常状态: " + ex.getStatusCode().value()))
                    .onErrorMap(IllegalArgumentException.class, ex ->
                            new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, ex.getMessage()))
                    .onErrorMap(java.util.concurrent.TimeoutException.class, ex ->
                            new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "获取下游 OpenAPI 文档超时"));
        });
    }

    private GatewayRoute findRoute(List<GatewayRoute> routes, String service, String group) {
        if (service == null || service.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "service 不能为空");
        }
        return routes.stream()
                .filter(route -> service.equals(route.getServiceName()))
                .filter(route -> Objects.equals(group, normalizeGroup(route.getGroup())))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到匹配的 OpenAPI 文档分组"));
    }

    private URI buildTargetUri(ServerWebExchange exchange, GatewayRoute route) {
        String routeUrl = route.getUrl();
        if (routeUrl == null || routeUrl.isBlank() || !routeUrl.startsWith("/")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAPI 文档地址必须是同源相对路径");
        }
        int queryStart = routeUrl.indexOf('?');
        String path = queryStart >= 0 ? routeUrl.substring(0, queryStart) : routeUrl;
        String query = queryStart >= 0 ? routeUrl.substring(queryStart + 1) : null;
        return UriComponentsBuilder.fromUri(exchange.getRequest().getURI())
                .replacePath(path)
                .replaceQuery(query)
                .build(true)
                .toUri();
    }

    private CachedSpec toCachedSpec(ResponseEntity<byte[]> response, URI targetUri) {
        byte[] body = response.getBody() == null ? new byte[0] : response.getBody();
        if (body.length > maxDocumentBytes(props)) {
            throw new IllegalArgumentException("OpenAPI 文档超过最大允许大小");
        }
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_JSON;
        }
        Instant lastModified = Instant.now();
        String etag = "\"" + sha256(body) + "\"";
        return new CachedSpec(body, contentType, etag, lastModified, targetUri.toString());
    }

    private ResponseEntity<byte[]> toResponse(ServerWebExchange exchange, CachedSpec spec) {
        String requestedEtag = exchange.getRequest().getHeaders().getFirst(HttpHeaders.IF_NONE_MATCH);
        if (spec.etag().equals(requestedEtag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(spec.etag())
                    .lastModified(spec.lastModified())
                    .cacheControl(CacheControl.noCache())
                    .build();
        }
        return ResponseEntity.ok()
                .contentType(spec.contentType())
                .contentLength(spec.body().length)
                .eTag(spec.etag())
                .lastModified(spec.lastModified())
                .cacheControl(CacheControl.noCache())
                .header("X-Springdoc-Plus-Document-Source", spec.source())
                .header("X-Springdoc-Plus-Document-Size", String.valueOf(spec.body().length))
                .body(spec.body());
    }

    private String normalizeGroup(String group) {
        if (group == null || group.isBlank() || DEFAULT_GROUP.equalsIgnoreCase(group)) {
            return DEFAULT_GROUP;
        }
        return group;
    }

    private String sha256(byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(body));
        } catch (Exception e) {
            throw new IllegalStateException("计算 OpenAPI 文档摘要失败", e);
        }
    }

    private Duration proxyTimeout() {
        Duration timeout = props.getProxy().getTimeout();
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(3) : timeout;
    }

    private Duration proxyCacheTtl() {
        Duration ttl = props.getProxy().getCache().getTtl();
        return ttl == null || ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(60) : ttl;
    }

    private long proxyCacheMaximumSize() {
        long maximumSize = props.getProxy().getCache().getMaximumSize();
        return maximumSize <= 0 ? 100 : maximumSize;
    }

    private static int maxDocumentBytes(SpringdocPlusGatewayProperties props) {
        long configured = props.getProxy().getMaxDocumentBytes();
        long normalized = configured <= 0 ? 5L * 1024L * 1024L : configured;
        return normalized > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) normalized;
    }

    private record ProxyCacheKey(String service, String group, String url) {
    }

    private record CachedSpec(byte[] body, MediaType contentType, String etag, Instant lastModified, String source) {
    }
}
