package com.sjl.rpc.context.spring.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketReference {
    Class<?> value() ;
    String name() default "";

    String version() default "";

    String group() default "";
}
