package com.github.rpc.context.spring.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketService {
    Class<?> value() ;

    String version() default "";
}
