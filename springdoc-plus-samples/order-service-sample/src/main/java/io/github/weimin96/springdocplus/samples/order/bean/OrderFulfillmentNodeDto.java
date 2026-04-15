package io.github.weimin96.springdocplus.samples.order.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 订单履约节点。
 * <p>
 * 该 DTO 用于验证 {@code CommonResult<List<T>>} 场景下数组元素的引用名称和字段展开能力。
 *
 * @author pwm
 */
@Schema(description = "订单履约节点")
public class OrderFulfillmentNodeDto {

    @Schema(description = "节点编码")
    private String nodeCode;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "处理人")
    private String operatorName;

    @Schema(description = "节点是否完成")
    private Boolean finished;

    /**
     * 无参构造器。
     */
    public OrderFulfillmentNodeDto() {
    }

    /**
     * 获取节点编码。
     *
     * @return 节点编码
     */
    public String getNodeCode() {
        return nodeCode;
    }

    /**
     * 设置节点编码。
     *
     * @param nodeCode 节点编码
     */
    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    /**
     * 获取节点名称。
     *
     * @return 节点名称
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * 设置节点名称。
     *
     * @param nodeName 节点名称
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * 获取处理人。
     *
     * @return 处理人
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * 设置处理人。
     *
     * @param operatorName 处理人
     */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    /**
     * 获取节点是否完成。
     *
     * @return 节点是否完成
     */
    public Boolean getFinished() {
        return finished;
    }

    /**
     * 设置节点是否完成。
     *
     * @param finished 节点是否完成
     */
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
}
