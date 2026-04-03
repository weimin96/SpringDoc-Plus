package io.github.weimin96.springdocplus.openapi3.properties;

import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import io.github.weimin96.springdocplus.core.model.SpringdocPlusGatewayAuth;
import io.github.weimin96.springdocplus.core.model.SpringdocPlusGatewayHttpBasic;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 单服务 OpenAPI 配置属性类。
 * <p>
 * 用于配置单服务场景下的分组信息，支持自定义分组名、URL、contextPath 等。
 *
 * @author pwm
 */
@Data
@ConfigurationProperties(prefix = SpringdocPlusOpenApi3Properties.PREFIX)
public class SpringdocPlusOpenApi3Properties {

    /**
     * 配置前缀
     */
    public static final String PREFIX = "springdoc-plus.openapi3";

    /**
     * 是否启用单服务 OpenAPI 文档
     */
    private boolean enabled = false;

    /**
     * 分组列表配置
     */
    private List<Group> groups = new ArrayList<>();

    /**
     * tag 排序策略
     */
    private GroupOrderStrategy tagsSorter = GroupOrderStrategy.alpha;

    /**
     * operation 排序策略
     */
    private GroupOrderStrategy operationsSorter = GroupOrderStrategy.alpha;

    /**
     * UI 认证配置
     */
    private SpringdocPlusGatewayAuth auth = new SpringdocPlusGatewayAuth();

    /**
     * Basic 提示配置
     */
    private SpringdocPlusGatewayHttpBasic basic = new SpringdocPlusGatewayHttpBasic();

    /**
     * 分组配置
     */
    @Data
    public static class Group {
        /**
         * 分组名称
         */
        private String name = "default";

        /**
         * OpenAPI 文档 URL
         */
        private String url = "/v3/api-docs";

        /**
         * 上下文路径（用于 Try it out 的 basePath）
         */
        private String contextPath = "/";

        /**
         * 排序值
         */
        private Integer order = 0;
    }

    /**
     * 无参构造器
     */
    public SpringdocPlusOpenApi3Properties() {
    }
}
