package com.sjl.rpc.context.netty.server;

import com.sjl.rpc.context.netty.abs.BaseServerTransporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * @author: jianlei
 * @date: 2019/11/30
 * @description: NettyServer
 */
@Component
@DependsOn("springBeanUtil")
@Slf4j
public class NettyServer extends BaseServerTransporter {

  //  @PostConstruct
  public static void start() {
      new NettyServer().bind();
  }

}
