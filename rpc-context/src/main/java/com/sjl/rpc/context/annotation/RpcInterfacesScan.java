package com.sjl.rpc.context.annotation;

import com.sjl.rpc.context.constants.Constant;
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
public  @interface RpcInterfacesScan {

    Class<?>[] basePackagesClasses() default {} ;

    /**
     *
     * @return 消费者 & 提供者
     */
    String type() default Constant.CONSUMER;


    String[] value() default {};

    String[] basePackages() default {};

    Class<? extends Annotation> annotationClass() default Annotation.class;
}
