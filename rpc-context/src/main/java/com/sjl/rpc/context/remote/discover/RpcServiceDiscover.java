package com.sjl.rpc.context.remote.discover;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.exception.RpcException;
import com.sjl.rpc.context.mode.RpcRequest;
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
public class RpcServiceDiscover extends BaseRpcHandler implements IRpcServiceDiscover {


  @Override
  public String selectService(RpcRequest request) {
    try {
      log.info("开始查找服务列表入参 request:{}", JSONObject.toJSONString(request));
      return getChildNodePath(request);
    } catch (Exception e) {
      log.error("发现服务异常", e);
      throw new RpcException("发现服务异常");
    }
  }
}
