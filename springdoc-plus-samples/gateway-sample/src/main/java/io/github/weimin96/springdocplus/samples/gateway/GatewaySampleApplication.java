package io.github.weimin96.springdocplus.samples.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关示例应用入口类。
 * <p>
 * 展示如何在 Spring Cloud Gateway 环境下使用 SpringDoc-Plus 网关聚合文档功能。
 *
 * @author pwm
 */
@SpringBootApplication
public class GatewaySampleApplication {

    /**
     * 应用入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewaySampleApplication.class, args);
    }
}
