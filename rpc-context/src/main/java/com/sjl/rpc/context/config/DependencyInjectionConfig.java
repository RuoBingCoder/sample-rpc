package com.sjl.rpc.context.config;

import com.sjl.rpc.context.annotation.Reference;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author: JianLei
 * @date: 2020/8/31 3:46 下午
 * @description:
 */
@Configuration
public class DependencyInjectionConfig {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 3)
    @Scope
    public static AutowiredAnnotationBeanPostProcessor beanPostProcessor() {
        AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
        beanPostProcessor.setAutowiredAnnotationType(Reference.class);
        return beanPostProcessor;
    }
}
