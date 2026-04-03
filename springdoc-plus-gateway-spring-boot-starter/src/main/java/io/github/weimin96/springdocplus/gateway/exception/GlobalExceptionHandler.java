package io.github.weimin96.springdocplus.gateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关统一异常处理器。
 * <p>
 * 处理所有未捕获的异常，返回统一的 JSON 错误响应。
 *
 * @author pwm
 */
@Order(-1)
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 记录异常日志
        logError(exchange, ex);

        // 根据异常类型设置状态码
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "内部服务器错误";

        if (ex instanceof ResponseStatusException responseStatusException) {
            status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            message = responseStatusException.getReason() != null
                    ? responseStatusException.getReason()
                    : ex.getMessage();
        } else if (ex.getCause() != null) {
            message = ex.getCause().getMessage();
        } else {
            message = ex.getMessage();
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(status);

        // 构建错误响应
        String errorResponse = String.format(
                "{\"code\":%d,\"message\":\"%s\",\"path\":\"%s\"}",
                status.value(),
                escapeJson(message),
                exchange.getRequest().getURI().getPath()
        );

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            return bufferFactory.wrap(errorResponse.getBytes());
        }));
    }

    /**
     * 记录错误日志
     */
    private void logError(ServerWebExchange exchange, Throwable ex) {
        String path = exchange.getRequest().getURI().getPath();

        if (ex instanceof ResponseStatusException) {
            log.warn("请求路径 {} 返回状态码 {}",
                    path,
                    ((ResponseStatusException) ex).getStatusCode().value());
        } else {
            log.error("请求路径 {} 发生未处理异常: {}",
                    path,
                    ex.getMessage(),
                    ex);
        }
    }

    /**
     * JSON 字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}