package com.github.rpc.context.remote.discover;

import com.alibaba.fastjson.JSONObject;
import com.github.rpc.context.exception.RocketException;
import com.github.rpc.context.remote.discover.abs.BaseRpcHandler;
import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.remote.discover.service.IRocketServiceDiscover;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author: JianLei
 * @date: 2020/9/6 2:39 下午
 * @description: 服务发现
 */
@Slf4j
@Component
public class RocketServiceDiscover extends BaseRpcHandler implements IRocketServiceDiscover, EnvironmentAware {

  private ConfigurableEnvironment env;

  @Override
  public String selectService(RocketRequest request) {
    try {
      log.info("开始查找服务列表入参 request:{}", JSONObject.toJSONString(request));
      return getChildNodePath(request);
    } catch (Exception e) {
      log.error("发现服务异常", e);
      throw new RocketException("发现服务异常,异常接口为:"+request.getClassName()+"."+request.getMethodName());
    }
  }

  @Override
  protected ConfigurableEnvironment getEnvironment() {
    return this.env;
  }

  @Override
  protected void registry(Class<?> serviceName, Object service, String version, Boolean isService) {

  }


  @Override
  public void setEnvironment(Environment environment) {
    if (environment instanceof ConfigurableEnvironment) {
      this.env = (ConfigurableEnvironment) environment;
    }
  }
}
