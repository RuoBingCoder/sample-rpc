package com.sjl.rpc.context.spring.event;

import cn.hutool.core.collection.CollectionUtil;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.netty.server.NettyServer;
import com.sjl.rpc.context.spring.annotation.EnableRocketScan;
import com.sjl.rpc.context.spring.scanner.RocketScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: JianLei
 * @date: 2020/6/13 10:13 下午
 * @description:
 */
@Slf4j
public class RpcRegistryListener implements ApplicationListener<ContextRefreshedEvent> {
  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
      try {
        final ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        final Environment env = applicationContext.getEnvironment();
        if (Constant.TCP_PROTOCOL.equals(env.getProperty(Constant.PROTOCOL))){
          final Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EnableRocketScan.class);
          if (CollectionUtil.isNotEmpty(beans)){
            NettyServer.start();
            log.info("netty server start success!");
          }
        }

      } catch (Exception e) {
        log.error("netty server start exception!", e);
      }
    }
  }


}