package com.sjl.rpc.context.spring.annotation;

import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.spring.registry.RocketRegistry;
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
@Import(RocketRegistry.class)
public  @interface EnableRocketScan {

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
