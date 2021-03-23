package com.github.rpc.context.remote.discover.service;

/**
 * @author: JianLei
 * @date: 2020/9/6 2:26 下午
 * @description:
 */
@Deprecated
public interface IServiceRegistry {


    void registry(Class<?> serviceName,String version) throws Exception;
}
