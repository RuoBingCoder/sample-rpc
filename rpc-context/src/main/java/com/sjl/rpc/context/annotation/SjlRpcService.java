package com.sjl.rpc.context.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SjlRpcService {
    Class<?> value() ;
}
