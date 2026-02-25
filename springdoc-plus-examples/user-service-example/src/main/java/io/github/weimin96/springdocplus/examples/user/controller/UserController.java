package io.github.weimin96.springdocplus.examples.user.controller;

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
@Tag(name = "用户")
@RestController
@RequestMapping("/users")
public class UserController {

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return Map.of("id", id, "name", "user-" + id);
    }
}
