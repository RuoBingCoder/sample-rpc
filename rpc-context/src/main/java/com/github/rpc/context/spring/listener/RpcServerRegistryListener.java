package com.github.rpc.context.spring.listener;

import cn.hutool.core.collection.CollectionUtil;
import com.github.rpc.context.spring.annotation.EnableRocketScan;
import com.github.rpc.context.constants.Constant;
import com.github.rpc.context.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

import java.lang.annotation.Documented;
import java.util.Map;
import java.util.Objects;

/**
 * @author: JianLei
 * @date: 2020/6/13 10:13 下午
 * @description:
 */
@Slf4j
@Deprecated
public class RpcServerRegistryListener implements ApplicationListener<ContextRefreshedEvent> {
  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    /*if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
      try {
        final ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        final Environment env = applicationContext.getEnvironment();
        if (Constant.TCP_PROTOCOL.equals(env.getProperty(Constant.PROTOCOL))){
          final Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EnableRocketScan.class);
          if (CollectionUtil.isNotEmpty(beans)){
            NettyServer.start(Integer.valueOf(Objects.requireNonNull(env.getProperty(Constant.PROTOCOL_PORT))));
            log.info("netty server start success!");
          }
        }

      } catch (Exception e) {
        log.error("netty server start exception!", e);
      }
    }*/
  }


}
