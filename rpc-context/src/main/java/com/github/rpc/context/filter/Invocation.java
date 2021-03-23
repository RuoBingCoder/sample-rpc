package com.github.rpc.context.filter;

/**
 * @author jianlei.shi
 * @date 2021/3/19 10:20 上午
 * @description: Invocation
 */
public interface Invocation {



    String getMethodName();

    String getClassName();

    Object getObj();

    Object[] getParameters();
}
