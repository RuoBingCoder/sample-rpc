package com.sjl.rpc.context.remote.discover;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.exception.RocketException;
import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.remote.handler.abs.BaseRpcHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: JianLei
 * @date: 2020/9/6 2:39 下午
 * @description: 服务发现
 */
@Slf4j
@Component
public class RocketServiceDiscover extends BaseRpcHandler implements IRocketServiceDiscover {


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
}
