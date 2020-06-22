package com.sjl.nettyclient.order.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: jianlei
 * @date: 2020/6/22
 * @description: NettyRemoteConfigHandler
 */
@Slf4j
public class NettyClientRemoteConfigHandler extends SimpleChannelInboundHandler<String> {

  private String config;

  @Override
  protected void channelRead0(ChannelHandlerContext cx, String s) throws Exception {
    this.config = s;
    log.info("========客户端获取配置中心配置:{}========", s);
    NettyClient.respConfig = config;
  }
}
