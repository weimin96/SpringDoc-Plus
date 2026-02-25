package io.github.weimin96.springdocplus.gateway.properties;


import io.github.weimin96.springdocplus.core.enums.GatewayStrategy;
import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import io.github.weimin96.springdocplus.core.enums.OpenApiVersion;
import io.github.weimin96.springdocplus.core.model.GatewayRoute;
import io.github.weimin96.springdocplus.core.model.SpringdocPlusGatewayAuth;
import io.github.weimin96.springdocplus.core.model.SpringdocPlusGatewayHttpBasic;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * @author pwm
 */
@Data
@ConfigurationProperties(prefix = SpringdocPlusGatewayProperties.PREFIX)
public class SpringdocPlusGatewayProperties {

    /**
     * 前缀
     */
    public static final String PREFIX = "springdoc-plus.gateway";

    /**
     * 是否开启
     */
    private boolean enabled = false;

    /**
     * 聚合策略
     */
    private GatewayStrategy strategy = GatewayStrategy.MANUAL;

    /**
     * tag 排序策略
     */
    private GroupOrderStrategy tagsSorter = GroupOrderStrategy.alpha;

    /**
     * operation 排序策略
     */
    private GroupOrderStrategy operationsSorter = GroupOrderStrategy.alpha;

    /**
     * Basic 保护（对齐 Knife4j Gateway basic）
     */
    private SpringdocPlusGatewayHttpBasic basic = new SpringdocPlusGatewayHttpBasic();

    /**
     * UI 侧鉴权（Bearer/Basic/自定义 Header 等）
     */
    private SpringdocPlusGatewayAuth auth = new SpringdocPlusGatewayAuth();

    /**
     * 手动路由配置（manual 模式主数据源；discover 模式用于补充/覆写）
     */
    private List<GatewayRoute> routes = new ArrayList<>();

    private final Discover discover = new Discover();

    @Data
    public static class Discover {
        private boolean enabled = false;

        private OpenApiVersion version = OpenApiVersion.OPENAPI3;

        /**
         * 需要排除的服务（exact 或 regex）
         */
        private Set<String> excludedServices = new HashSet<>();

        /**
         * 默认 OpenAPI3 文档地址（下游服务统一使用 default 分组）
         */
        private String openapi3Url = "/v3/api-docs";

        /**
         * 每个服务个性化配置：key=serviceId
         */
        private Map<String, ServiceConfig> serviceConfig = new HashMap<>();

        /**
         * discover 模式下：是否尝试解析 Gateway route predicates 以推断 contextPath。
         * 默认 true。
         */
        private boolean resolveContextPathFromGatewayRoutes = true;
    }

    @Data
    public static class ServiceConfig {
        private Integer order = 0;
        private String groupName;

        /**
         * 兼容 OpenAPI3 聚合时 contextPath 丢失，或用于 Try it out 的 basePath
         */
        private String contextPath;

        /**
         * 子服务额外分组（对齐 Knife4j 4.2+ 的 group-names）。
         * 若配置，将为每个 group 生成一个聚合入口。
         */
        private List<String> groupNames;
    }
}
