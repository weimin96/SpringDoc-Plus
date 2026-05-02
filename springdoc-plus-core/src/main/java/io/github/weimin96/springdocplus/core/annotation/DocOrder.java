package io.github.weimin96.springdocplus.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SpringDoc-Plus 排序注解。
 * <p>
 * 用于替代手写 {@code x-order} 扩展字段，降低从 Knife4j 排序注解迁移的成本。
 *
 * @author pwm
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocOrder {

    /**
     * 排序值，数值越小越靠前。
     *
     * @return 排序值
     */
    int value();
}
