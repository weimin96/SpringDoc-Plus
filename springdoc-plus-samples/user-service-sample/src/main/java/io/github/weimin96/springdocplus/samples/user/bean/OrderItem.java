package io.github.weimin96.springdocplus.samples.user.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 订单项
 *
 * @author pwm
 * @since 2026/3/2 16:24
 */
@Schema(description = "订单项")
public class OrderItem {
    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "单价")
    private Double price;

    /**
     * 无参构造器
     */
    public OrderItem() {
    }

    // Getters and Setters

    /**
     * 获取商品ID
     *
     * @return 商品ID
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * 设置商品ID
     *
     * @param productId 商品ID
     */
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /**
     * 获取商品名称
     *
     * @return 商品名称
     */
    public String getProductName() {
        return productName;
    }

    /**
     * 设置商品名称
     *
     * @param productName 商品名称
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * 获取数量
     *
     * @return 数量
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * 设置数量
     *
     * @param quantity 数量
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 获取单价
     *
     * @return 单价
     */
    public Double getPrice() {
        return price;
    }

    /**
     * 设置单价
     *
     * @param price 单价
     */
    public void setPrice(Double price) {
        this.price = price;
    }
}