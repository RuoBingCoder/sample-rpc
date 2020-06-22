package com.sjl.nettyserver.goods.server;

import com.google.gson.Gson;
import com.sjl.nettyserver.goods.util.PropertiesUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author: jianlei
 * @date: 2020/6/22
 * @description: NettyRemoteConfigCenterHandler
 */
@Slf4j
public class NettyServerRemoteConfigCenterHandler extends SimpleChannelInboundHandler<String> {
  @Override
  protected void channelRead0(ChannelHandlerContext cx, String s) throws Exception {
    log.info("开始获取配置中心配置:{}", s == null ? "--" : s);
    try {
      Map<String, Object> configProperties = null;
      if (s != null) {
        configProperties = PropertiesUtil.getSjlAllConfigProperties(s);
      }

      cx.channel().writeAndFlush(new Gson().toJson(configProperties)).addListener(ChannelFutureListener.CLOSE);

    } catch (Exception e) {
      log.error("获取配置中心出现异常", e);
    }
  }
}
