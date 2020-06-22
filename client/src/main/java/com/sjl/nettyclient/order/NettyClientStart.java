package com.sjl.nettyclient.order;

import com.sjl.nettyclient.order.client.NettyClient;
import com.sjl.nettyclient.order.util.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author: JianLei
 * @date: 2020/6/13 10:20 下午
 * @description:
 */

/*@Component
@Slf4j
public class NettyClientStart implements ApplicationListener<ContextRefreshedEvent> {
  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
      try {

        log.info("******获取配置中心配置*******");
        String config = (String) ThreadPoolUtil.submit(() -> NettyClient.remoteConfigStart("config"));
          log.info("******获取配置中心配置结束:{}*******",config);

      } catch (Exception e) {
        log.error("netty client start exception!", e);
      }
    }
  }
}*/

