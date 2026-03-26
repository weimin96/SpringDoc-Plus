package io.github.weimin96.springdocplus.core.enums;

/**
 * 分组排序策略枚举。
 * <p>
 * 对齐 Knife4j 的排序策略：
 * <ul>
 *   <li>alpha - 使用字母顺序排序（swagger-ui 默认行为）</li>
 *   <li>order - 使用 x-order 扩展字段排序（本项目在 UI 侧提供可插拔 hook）</li>
 * </ul>
 *
 * @author pwm
 */
public enum GroupOrderStrategy {
    /**
     * 按字母顺序排序
     */
    alpha,
    /**
     * 按 x-order 扩展字段排序
     */
    order
}
