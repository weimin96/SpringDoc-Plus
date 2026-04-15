package io.github.weimin96.springdocplus.samples.order.controller;

import io.github.weimin96.springdocplus.samples.order.bean.CommonResult;
import io.github.weimin96.springdocplus.samples.order.bean.OrderDocumentDto;
import io.github.weimin96.springdocplus.samples.order.bean.OrderFulfillmentNodeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器。
 * <p>
 * 提供订单相关的 REST 接口。
 *
 * @author pwm
 */
@Tag(name = "订单")
@RestController
@RequestMapping("/orders")
public class OrderController {

    /**
     * 无参构造器
     */
    public OrderController() {
    }

    /**
     * 根据 ID 获取订单
     *
     * @param id id
     * @return Map
     */
    @Operation(summary = "根据 ID 获取订单")
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return Map.of("id", id, "amount", 99.9, "status", "PAID");
    }

    /**
     * 获取订单文档视图。
     *
     * @param id 订单ID
     * @return 统一响应结果
     */
    @Operation(
            summary = "获取订单文档视图"
    )
    @GetMapping("/{id}/document-view")
    public CommonResult<OrderDocumentDto> getDocumentView(@PathVariable Long id) {
        OrderDocumentDto documentDto = new OrderDocumentDto();
        documentDto.setId(id);
        documentDto.setOrderNo("SO-" + id);
        documentDto.setCustomerName("华东采购中心");
        documentDto.setPayableAmount(new BigDecimal("2680.50"));
        documentDto.setStatus("WAITING_DELIVERY");
        return buildSuccessResult(documentDto, "trace-order-document-" + id);
    }

    /**
     * 获取订单履约节点列表。
     *
     * @param id 订单ID
     * @return 统一响应结果
     */
    @Operation(
            summary = "获取订单履约节点列表"
    )
    @GetMapping("/{id}/fulfillment-nodes")
    public CommonResult<List<OrderFulfillmentNodeDto>> listFulfillmentNodes(@PathVariable Long id) {
        OrderFulfillmentNodeDto pickingNode = new OrderFulfillmentNodeDto();
        pickingNode.setNodeCode("PICKING");
        pickingNode.setNodeName("拣货完成");
        pickingNode.setOperatorName("仓内调度员");
        pickingNode.setFinished(Boolean.TRUE);

        OrderFulfillmentNodeDto shippingNode = new OrderFulfillmentNodeDto();
        shippingNode.setNodeCode("SHIPPING");
        shippingNode.setNodeName("发货待确认");
        shippingNode.setOperatorName("物流专员");
        shippingNode.setFinished(Boolean.FALSE);

        return buildSuccessResult(List.of(pickingNode, shippingNode), "trace-order-fulfillment-" + id);
    }

    /**
     * 统一构造成功响应。
     * <p>
     * 单独抽出该方法是为了让两个验证接口共享完全一致的包装结构，
     * 避免样例之间出现额外差异，影响对文档渲染问题的判断。
     *
     * @param data    业务数据
     * @param traceId 链路追踪标识
     * @param <T>     业务数据类型
     * @return 统一响应结果
     */
    private <T> CommonResult<T> buildSuccessResult(T data, String traceId) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(200);
        result.setMsg("处理成功");
        result.setData(data);
        result.setTraceId(traceId);
        return result;
    }
}
