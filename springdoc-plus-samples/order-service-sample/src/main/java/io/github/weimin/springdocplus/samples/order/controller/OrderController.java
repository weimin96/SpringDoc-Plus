package io.github.weimin.springdocplus.samples.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
