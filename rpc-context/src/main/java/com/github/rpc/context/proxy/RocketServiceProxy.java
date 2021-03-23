package com.github.rpc.context.proxy;

import com.github.rpc.context.spring.annotation.RocketReferenceAttribute;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 代理类
 *
 * @param <T>
 */
@Slf4j
public class RocketServiceProxy<T> extends AbsRocketSupport {

    private T target;
    private RocketReferenceAttribute referenceAttribute;

    public RocketServiceProxy(T target, RocketReferenceAttribute referenceAttribute) {
        this.target = target;
        this.referenceAttribute = referenceAttribute;
    }

    public RocketServiceProxy(T target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object res = doInvoke(referenceAttribute, method, args);
        return res;


    }


}
