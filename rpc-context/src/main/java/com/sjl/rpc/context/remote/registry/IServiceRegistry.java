package com.sjl.rpc.context.remote.registry;

/**
 * @author: JianLei
 * @date: 2020/9/6 2:26 下午
 * @description:
 */

public interface IServiceRegistry {


    void registry(Class<?> serviceName,String version) throws Exception;
}
