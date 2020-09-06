package com.sjl.rpc.context.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.codec.RpcDecoder;
import com.sjl.rpc.context.codec.RpcEncoder;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.mode.RpcResponse;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.remote.discover.RpcServiceDiscover;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: jianlei
 * @date: 2019/12/1
 * @description: NettyClient
 */
@Component
@Slf4j
public class NettyClient {

  public static RpcResponse rpcResponse;

  public static RpcResponse start(RpcRequest request) {

    EventLoopGroup loopGroup = new NioEventLoopGroup();
    // 客户端使用的是Bootstrap

    try {

      Bootstrap bootstrap = new Bootstrap();

      bootstrap
          .group(loopGroup)
          .channel(NioSocketChannel.class)
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                  socketChannel
                      .pipeline()
                      /*将RPC请求进行编码（发送请求）*/
                      .addLast(new RpcEncoder(RpcRequest.class))
                      /*将RPC响应进行解码（返回响应）*/
                      .addLast(new RpcDecoder(RpcResponse.class))
                      /*使用NettyClientHandler发送RPC请求*/
                      .addLast(new NettyClientHandler());

                }
              });
      RpcServiceDiscover rpcServiceDiscover= SpringBeanUtil.getBean(RpcServiceDiscover.class);
      final String[] providerHost =  rpcServiceDiscover.selectService(request).split(":");
      log.info("获取远程服务地址是:{}", JSONObject.toJSONString(providerHost));
      ChannelFuture channelFuture = bootstrap.connect(providerHost[0], Integer.parseInt(providerHost[1])).sync();
      channelFuture.channel().writeAndFlush(request).sync();
      channelFuture.channel().closeFuture().sync();

    } catch (Exception e) {
      e.printStackTrace();

    } finally {
      loopGroup.shutdownGracefully();
    }
    return rpcResponse == null ? new RpcResponse() : rpcResponse;
  }


}
