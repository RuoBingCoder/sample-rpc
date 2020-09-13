package com.sjl.rpc.context.proxy;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.annotation.RocketReferenceAttribute;
import com.sjl.rpc.context.exception.RocketException;
import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import com.sjl.rpc.context.netty.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 代理类
 *
 * @param <T>
 */
@Slf4j
public class RocketServiceProxy<T> implements InvocationHandler {

  private T target;
  private RocketReferenceAttribute referenceAttribute;

  public RocketServiceProxy(T target, RocketReferenceAttribute referenceAttribute) {
    this.target = target;
    this.referenceAttribute = referenceAttribute;
  }

  public RocketServiceProxy(T target) {
    this.target = target;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String className = method.getDeclaringClass().getName();
    String methodName = method.getName();
    String version = referenceAttribute.getVersion();
    log.info("invoke params is:{}",className+"--"+methodName+"--"+version);
    try {
        RocketRequest request = new RocketRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        request.setVersion(version);
        RocketResponse response = NettyClient.start(request);
        if (log.isDebugEnabled()){
          log.debug("remote request params is:{} ", JSONObject.toJSONString(request));
        }
        log.info("-->>>>>>>>response result is:{}",response.getResult());
        return response.getResult();
    } catch (Exception e) {
      log.error("远程调用服务类:[" + className + "]" + "-方法[" + methodName + "]异常!异常信息是:",e);
      throw new RocketException("远程调用服务类:[" + className + "]" + "-方法[" + methodName + "]异常!");
    }
  }
}
