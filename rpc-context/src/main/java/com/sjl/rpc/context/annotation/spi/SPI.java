package com.sjl.rpc.context.annotation.spi;


import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPI {

    String name() default "";

}
