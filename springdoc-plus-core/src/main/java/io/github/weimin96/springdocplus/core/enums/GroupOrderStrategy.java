package io.github.weimin96.springdocplus.core.enums;

/**
 * 对齐 Knife4j：
 * - alpha：swagger-ui 默认排序
 * - order：使用 x-order/Knife4j 扩展排序（本项目 MVP+ 先在 UI 侧提供可插拔 hook）
 */
public enum GroupOrderStrategy {
  alpha,
  order
}
