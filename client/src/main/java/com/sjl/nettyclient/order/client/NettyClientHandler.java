package com.sjl.nettyclient.order.client;

import api.mode.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: jianlei
 * @date: 2019/12/1
 * @description: NettyClientHandler
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {


  private RpcResponse response;


  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
    this.response = rpcResponse;
    //简单赋值
    NettyClient.rpcResponse = rpcResponse;
    log.info("客户端收到服务器相应：{}",response.toString());
  }


}
