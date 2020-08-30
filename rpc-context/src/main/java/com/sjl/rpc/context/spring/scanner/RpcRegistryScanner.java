package com.sjl.rpc.context.spring.scanner;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.factory.RpcServiceBeanDefinitionRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: JianLei
 * @date: 2020/8/30 2:24 下午
 * @description: 对标有rpcService注解的类进行注册到ioc中
 */
@Slf4j
public class RpcRegistryScanner extends ClassPathBeanDefinitionScanner {

    private String basePackage;
    private String beanName;
    private Class<? extends Annotation> annotationClass;
    private String type;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public RpcRegistryScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public int scan(String... basePackages) {
        log.info("=======>>>basePackages :{}<<========", JSONObject.toJSONString(basePackages));

        List<String> strings = Arrays.asList(basePackages);
        if (strings.contains("consumer")){
            log.info("consumer package is :{}", JSONObject.toJSONString(basePackages));
            RpcServiceBeanDefinitionRegistry.scannerPackages= strings.stream().filter(s -> s.startsWith("api")).collect(Collectors.joining());
            return 1;
        }
        return super.scan(basePackages);
    }
}
