package com.sjl.nettyclient.order.proxy;

import api.mode.RpcRequest;
import api.mode.RpcResponse;
import com.sjl.nettyclient.order.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * 代理类
 * @param <T>
 */
@Slf4j
public class RpcServiceProxy<T> implements InvocationHandler {

  private T target;


  public RpcServiceProxy(T target) {
    this.target = target;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    System.out.println(method.getName()+"  "+method.getDeclaringClass().getName()+"   "+ Arrays.deepToString(args));
    String className = method.getDeclaringClass().getName();
    String methodName = method.getName();

    RpcRequest request = new RpcRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setClassName(className);
    request.setMethodName(methodName);
    request.setParameters(args);
    request.setParameterTypes(method.getParameterTypes());
    RpcResponse response = NettyClient.rpcStart(request);

    log.info("-->>>>>>>>:{}",response.getResult());

    return response.getResult();
  }
}
