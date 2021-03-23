package com.github.rpc.context.filter;

import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.spring.annotation.spi.ExtensionLoader;
import org.springframework.cglib.reflect.FastMethod;

/**
 * @author jianlei.shi
 * @date 2021/3/19 10:35 上午
 * @description DelegateHandle
 */

public class DelegateHandle {

    public RocketResponse invoke(RocketResponse response, FastMethod method, Object object, Object[] params){
        final ExtensionLoader extensionLoader = ExtensionLoader.getExtensionLoader(RocketFilter.class);
        final RocketFilter filter = (RocketFilter) extensionLoader.getExtension("filter");
        if (filter == null) {
            return null;
        }
        Invoker invoker=buildInvoker(method,response);
        Invocation invocation=new RocketInvocation(null,null,object,params);
        final RocketResponse resp = filter.invoke(invoker, invocation);
        return resp;

    }

    private Invoker buildInvoker(FastMethod method, RocketResponse response) {
        Invoker invoker=new RocketInvoker();
        invoker.setMethod(method);
        invoker.setResponse(response);
        return invoker;
    }

}
