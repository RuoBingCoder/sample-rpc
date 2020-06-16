package com.sjl.nettyserver.goods;

import com.sjl.nettyserver.goods.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author: JianLei
 * @date: 2020/6/13 10:13 下午
 * @description:
 */
@Component
@Slf4j
public class NettyStart implements ApplicationListener<ContextRefreshedEvent> {
  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
      try {
        NettyServer.start();
        log.info("netty server start success!");

      } catch (Exception e) {
        log.error("netty server start exception!", e);
      }
    }
  }
}
