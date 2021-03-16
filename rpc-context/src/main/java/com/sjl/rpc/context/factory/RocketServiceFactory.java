package com.sjl.rpc.context.factory;

import com.sjl.rpc.context.spring.annotation.RocketReferenceAttribute;
import com.sjl.rpc.context.proxy.RocketServiceProxy;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author shijianlei
 * @date: 2020/9/13
 * 代理工厂类(因为重构现在实现FactoryBean只是为了获取bean)
 * @param <T>
 */
public class RocketServiceFactory<T> implements FactoryBean<T> {

    private  Class<T> interfaceType;

    private RocketReferenceAttribute referenceAttribute;

    public RocketServiceFactory(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    public RocketServiceFactory(Class<T> interfaceType, RocketReferenceAttribute referenceAttribute) {
        this.interfaceType = interfaceType;
        this.referenceAttribute = referenceAttribute;
    }

    public RocketServiceFactory() {
    }

    @Override
    public T getObject() {
        InvocationHandler handler = new RocketServiceProxy<>(interfaceType,referenceAttribute);
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(),
                new Class[]{interfaceType}, handler);
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}