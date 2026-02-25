package io.github.weimin.springdocplus.examples.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author pwm
 */
@Tag(name = "订单")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Operation(summary = "根据ID获取订单")
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return Map.of("id", id, "amount", 99.9, "status", "PAID");
    }
}
