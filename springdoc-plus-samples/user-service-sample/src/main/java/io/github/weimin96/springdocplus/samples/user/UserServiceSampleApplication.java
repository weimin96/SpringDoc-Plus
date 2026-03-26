package io.github.weimin96.springdocplus.samples.user;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author pwm
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "用户服务", version = "v1"))
public class UserServiceSampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceSampleApplication.class, args);
    }
}
