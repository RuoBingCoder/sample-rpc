package com.sjl.nettyserver.goods.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author: jianlei
 * @date: 2020/6/19
 * @description: SjlAutowired
 */
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SjlAutowired {
  String value() default "";

  Class<?> name() default String.class;
}
