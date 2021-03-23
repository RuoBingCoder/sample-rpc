package com.github.rpc.context.filter;

import com.github.rpc.context.bean.RocketResponse;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.Method;

/**
 * @author jianlei.shi
 * @date 2021/3/19 10:22 上午
 * @description: Invoker
 */
public interface Invoker {


    void setMethod(FastMethod method);



    RocketResponse invoke(Invocation invocation);


    void setResponse(RocketResponse response);
}
