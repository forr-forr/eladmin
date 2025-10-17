package com.itheima.config.Operation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 注解作用于方法
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
public @interface NotLog {
    // 可以添加可选属性，例如描述原因
    String value() default "";
}