package com.sjl.rpc.context.proxy;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.exception.RpcException;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.mode.RpcResponse;
import com.sjl.rpc.context.netty.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * 代理类
 *
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
    String className = method.getDeclaringClass().getName();
    String methodName = method.getName();

    try {
      if (Constant.CACHE_SERVICE_ATTRIBUTES_MAP.containsKey(className)) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        request.setVersion(
            (String) Constant.CACHE_SERVICE_ATTRIBUTES_MAP.get(className).get("version"));
        RpcResponse response = NettyClient.start(request);
        if (log.isDebugEnabled()){
          log.debug("remote request params is:{} ", JSONObject.toJSONString(request));
        }
        log.info("-->>>>>>>>response result is:{}",response.getResult());
        return response.getResult();
      }
    } catch (Exception e) {
      log.error("远程调用服务类:[" + className + "]" + "-方法[" + methodName + "]异常!异常信息是:",e);
      throw new RpcException("远程调用服务类:[" + className + "]" + "-方法[" + methodName + "]异常!");
    }
    return null;
  }
}
