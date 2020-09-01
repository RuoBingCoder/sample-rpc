package com.sjl.rpc.context.proxy;

import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.mode.RpcResponse;
import com.sjl.rpc.context.netty.client.NettyClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * 代理类
 * @param <T>
 */
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
    System.out.println("======map size:"+Constant.CACHE_SERVICE_ATTRIBUTES_MAP.size());
    Constant.CACHE_SERVICE_ATTRIBUTES_MAP.forEach((key, value) -> System.out.println("[" + key + "->" + value.toString() + "]"));
    if (Constant.CACHE_SERVICE_ATTRIBUTES_MAP.containsKey(className)){
      RpcRequest request = new RpcRequest();
      request.setRequestId(UUID.randomUUID().toString());
      request.setClassName(className);
      request.setMethodName(methodName);
      request.setParameters(args);
      request.setParameterTypes(method.getParameterTypes());
      request.setVersion((String) Constant.CACHE_SERVICE_ATTRIBUTES_MAP.get(className).get("version"));
      RpcResponse response = NettyClient.start(request);
      System.out.println("-->>>>>>>>"+response.getResult());
      return response.getResult();
    }
    throw new RuntimeException("引用接口不存在");


  }
}
