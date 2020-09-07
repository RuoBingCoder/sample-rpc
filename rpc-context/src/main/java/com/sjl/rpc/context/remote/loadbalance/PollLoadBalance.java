package com.sjl.rpc.context.remote.loadbalance;

import com.sjl.rpc.context.remote.handler.abs.BaseLoadBalance;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: jianlei
 * @date: 2020/9/2
 * @description: 负载均衡
 */
@Component
public class PollLoadBalance extends BaseLoadBalance {

  private static int i = 0;

  @Override
  public <T> String selectService(T datas) {
    String result = "";
    if (datas instanceof List) {
      List services = (List) datas;
      if (i >= services.size()) {
        i = 0;
      }
      result = (String) services.get(i++);

    } else if (datas instanceof String) {
      result = (String) datas;
    }
    return result;
  }
}
