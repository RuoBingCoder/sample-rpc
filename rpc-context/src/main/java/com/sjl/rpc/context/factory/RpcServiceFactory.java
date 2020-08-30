package com.sjl.rpc.context.factory;

import com.sjl.rpc.context.proxy.RpcServiceProxy;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class RpcServiceFactory<T> implements FactoryBean<T> {

    private final Class<T> interfaceType;

    public RpcServiceFactory(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public T getObject() {
        InvocationHandler handler = new RpcServiceProxy<>(interfaceType);
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