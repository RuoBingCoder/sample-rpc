package com.github.rpc.context.filter;

import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.exception.RocketException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.Method;

/**
 * @author jianlei.shi
 * @date 2021/3/19 10:25 上午
 * @description RocketInvoker
 */
@Slf4j
public class RocketInvoker implements Invoker{

    private FastMethod method;
    private RocketResponse response;

    @Override
    public void setMethod(FastMethod method) {
        this.method = method;
    }

    @Override
    public RocketResponse invoke(Invocation invocation) {
        if (method==null){
            throw new RocketException("method is not null");
        }
         try {
           Object result= method.invoke(invocation.getObj(),invocation.getParameters());
           response.setResult(result);
           return response;
         } catch (Exception e) {
            log.error("RocketInvoker invoker error",e);
            throw new RocketException("RocketInvoker invoker error");

         }
    }

    @Override
    public void setResponse(RocketResponse response) {
        this.response = response;
    }
}
