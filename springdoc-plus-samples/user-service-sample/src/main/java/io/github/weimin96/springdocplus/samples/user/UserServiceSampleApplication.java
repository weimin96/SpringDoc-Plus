package io.github.weimin96.springdocplus.samples.user;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户服务示例应用入口类。
 * <p>
 * 展示如何在单服务环境下使用 SpringDoc-Plus 文档功能。
 *
 * @author pwm
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "用户服务", version = "v1"))
public class UserServiceSampleApplication {

    /**
     * 应用入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceSampleApplication.class, args);
    }
}
