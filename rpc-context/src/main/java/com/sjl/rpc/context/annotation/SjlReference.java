package com.sjl.rpc.context.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SjlReference {
    Class<?> value() ;
    String name() default "";

    String version() default "";
}
