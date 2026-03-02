package io.github.weimin96.springdocplus.examples.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * @author pwm
 */
@Tag(name = "用户", description = "用户接口")
@RestController
@RequestMapping("/users")
public class UserController {

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("id", id, "name", "user-" + id));
    }

    @Operation(summary = "新增用户")
    @PostMapping("/addUser")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        return ResponseEntity.ok(user);
    }

    class User {

        public User(String name) {
            this.name = name;
        }

        @Schema(description = "用户名")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
