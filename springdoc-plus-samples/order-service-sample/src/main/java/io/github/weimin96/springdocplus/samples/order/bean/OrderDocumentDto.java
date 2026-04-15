package io.github.weimin96.springdocplus.samples.order.bean;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 订单文档视图。
 * <p>
 * 该 DTO 保持字段简单且具备领域含义，便于在文档页直接观察 data 引用对象的字段展开效果。
 *
 * @author pwm
 */
@Schema(description = "订单文档视图")
public class OrderDocumentDto {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "下单客户名称")
    private String customerName;

    @Schema(description = "应付金额")
    private BigDecimal payableAmount;

    @Schema(description = "订单状态")
    private String status;

    /**
     * 无参构造器。
     */
    public OrderDocumentDto() {
    }

    /**
     * 获取订单ID。
     *
     * @return 订单ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置订单ID。
     *
     * @param id 订单ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取订单编号。
     *
     * @return 订单编号
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * 设置订单编号。
     *
     * @param orderNo 订单编号
     */
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    /**
     * 获取下单客户名称。
     *
     * @return 下单客户名称
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * 设置下单客户名称。
     *
     * @param customerName 下单客户名称
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * 获取应付金额。
     *
     * @return 应付金额
     */
    public BigDecimal getPayableAmount() {
        return payableAmount;
    }

    /**
     * 设置应付金额。
     *
     * @param payableAmount 应付金额
     */
    public void setPayableAmount(BigDecimal payableAmount) {
        this.payableAmount = payableAmount;
    }

    /**
     * 获取订单状态。
     *
     * @return 订单状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置订单状态。
     *
     * @param status 订单状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
