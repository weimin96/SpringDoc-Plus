package io.github.weimin96.springdocplus.openapi3.customizer;

import io.github.weimin96.springdocplus.core.annotation.DocOrder;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

/**
 * 将 {@link DocOrder} 转换为 OpenAPI 的 {@code x-order} 扩展字段。
 * <p>
 * 方法级注解优先于类型级注解，原因是同一个控制器下的接口通常需要独立排序。
 *
 * @author pwm
 */
public class DocOrderOperationCustomizer implements OperationCustomizer {

    private static final String X_ORDER = "x-order";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        DocOrder docOrder = findDocOrder(handlerMethod);
        if (docOrder == null) {
            return operation;
        }
        operation.addExtension(X_ORDER, docOrder.value());
        return operation;
    }

    private DocOrder findDocOrder(HandlerMethod handlerMethod) {
        DocOrder methodOrder = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), DocOrder.class);
        if (methodOrder != null) {
            return methodOrder;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), DocOrder.class);
    }
}
