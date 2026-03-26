package io.github.weimin96.springdocplus.samples.user.bean;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单信息
 *
 * @author pwm
 * @since 2026/3/2 16:23
 */
@Schema(description = "订单信息")
public class Order {
    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "用户信息")
    private User user;

    @Schema(description = "订单项列表")
    private List<OrderItem> items;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 无参构造器
     */
    public Order() {
    }

    // Getters and Setters

    /**
     * 获取订单ID
     *
     * @return 订单ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置订单ID
     *
     * @param id 订单ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取订单编号
     *
     * @return 订单编号
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * 设置订单编号
     *
     * @param orderNo 订单编号
     */
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    public User getUser() {
        return user;
    }

    /**
     * 设置用户信息
     *
     * @param user 用户信息
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 获取订单项列表
     *
     * @return 订单项列表
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * 设置订单项列表
     *
     * @param items 订单项列表
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}