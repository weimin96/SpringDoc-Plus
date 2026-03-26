package io.github.weimin96.springdocplus.core.enums;

/**
 * 网关聚合策略枚举。
 * <p>
 * 定义网关文档聚合的两种模式：
 * <ul>
 *   <li>MANUAL - 手动配置模式</li>
 *   <li>DISCOVER - 服务发现模式</li>
 * </ul>
 *
 * @author pwm
 */
public enum GatewayStrategy {
    /**
     * 手动配置模式，通过配置文件手动指定各服务的路由信息
     */
    MANUAL,
    /**
     * 服务发现模式，基于服务注册中心自动发现并聚合文档
     */
    DISCOVER
}
