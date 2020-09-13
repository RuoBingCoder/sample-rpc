package com.sjl.client.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author: JianLei
 * @date: 2020/6/13 10:20 下午
 * @description:
 */
@Component
@Slf4j
@Deprecated
public class NettyClientStart implements ApplicationListener<ContextRefreshedEvent> {
  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    /*if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
      try {
        NettyClient.start("hello netty");
      } catch (Exception e) {
        log.error("netty client start exception!", e);
      }
    }*/
  }
}
