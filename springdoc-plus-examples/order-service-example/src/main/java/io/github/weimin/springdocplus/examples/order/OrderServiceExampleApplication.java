package io.github.weimin.springdocplus.examples.order;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author pwm
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "订单服务", version = "v1"))
public class OrderServiceExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceExampleApplication.class, args);
    }
}
