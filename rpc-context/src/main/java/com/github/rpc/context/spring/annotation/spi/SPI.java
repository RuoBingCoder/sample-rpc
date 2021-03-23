package com.github.rpc.context.spring.annotation.spi;


import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPI {

    String name() default "";

}
