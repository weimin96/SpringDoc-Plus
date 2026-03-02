package io.github.weimin96.springdocplus.examples.user.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
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

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
