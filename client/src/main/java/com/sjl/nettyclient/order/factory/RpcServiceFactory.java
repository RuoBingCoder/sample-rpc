package com.sjl.nettyclient.order.factory;

import com.sjl.nettyclient.order.proxy.RpcServiceProxy;
import org.apache.naming.factory.webservices.ServiceProxy;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class RpcServiceFactory<T> implements FactoryBean<T> {

    private Class<T> interfaceType;

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