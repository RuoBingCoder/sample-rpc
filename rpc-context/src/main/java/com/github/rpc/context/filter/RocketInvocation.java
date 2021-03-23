package com.github.rpc.context.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jianlei.shi
 * @date 2021/3/19 10:31 上午
 * @description InvocationImpl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RocketInvocation implements Invocation{

    private String methodName;
    private String className;
    private Object obj;
    private Object[] parameters;



    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Object getObj() {
        return obj;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }
}
