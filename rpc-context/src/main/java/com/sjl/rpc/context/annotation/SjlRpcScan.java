package com.sjl.rpc.context.annotation;

import com.sjl.rpc.context.spring.registry.RpcRegistry;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author: JianLei
 * @date: 2020/8/30 2:18 下午
 * @description:
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RpcRegistry.class)
public  @interface SjlRpcScan {

    Class<?>[] basePackagesClasses() default {} ;

    String type() default "";


    String[] value() default {};

    String[] basePackages() default {};

    Class<? extends Annotation> annotationClass() default Annotation.class;
}
