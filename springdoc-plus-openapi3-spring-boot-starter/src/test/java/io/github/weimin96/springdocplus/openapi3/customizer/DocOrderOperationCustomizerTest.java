package io.github.weimin96.springdocplus.openapi3.customizer;

import io.github.weimin96.springdocplus.core.annotation.DocOrder;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;

class DocOrderOperationCustomizerTest {

    private final DocOrderOperationCustomizer customizer = new DocOrderOperationCustomizer();

    @Test
    void methodAnnotationOverridesTypeAnnotation() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(new DemoController(), "methodOrder");

        Operation operation = customizer.customize(new Operation(), handlerMethod);

        assertThat(operation.getExtensions()).containsEntry("x-order", 2);
    }

    @Test
    void typeAnnotationAppliesWhenMethodMissing() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(new DemoController(), "typeOrder");

        Operation operation = customizer.customize(new Operation(), handlerMethod);

        assertThat(operation.getExtensions()).containsEntry("x-order", 10);
    }

    @DocOrder(10)
    static class DemoController {

        @DocOrder(2)
        public void methodOrder() {
        }

        public void typeOrder() {
        }
    }
}
