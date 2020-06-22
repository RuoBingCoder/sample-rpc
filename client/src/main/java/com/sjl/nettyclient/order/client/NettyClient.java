package com.sjl.nettyclient.order.client;

import api.mode.RpcRequest;
import api.mode.RpcResponse;
import com.sjl.nettyclient.order.codec.RpcDecoder;
import com.sjl.nettyclient.order.codec.RpcEncoder;
import com.sjl.nettyclient.order.zk.ZkDiscovery;
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
  public static String respConfig;

  public static RpcResponse rpcStart(RpcRequest request) {

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

                  // 加入自己的处理器
                }
              });
      System.out.println("客户端ok...");
      // 获取注册地址
      final String addr = new ZkDiscovery().getService();
      String[] split = new String[0];
      ChannelFuture channelFuture;
      if (addr.contains(":")) {
        split = addr.split(":");
        log.info("******客户端从ZK获取服务地址是:{}******", addr);
        // 启动客户端连接服务器端
        channelFuture = bootstrap.connect(split[0], Integer.parseInt(split[1])).sync();
      } else {
        // 启动客户端连接服务器端
        channelFuture = bootstrap.connect("127.0.0.1", Integer.parseInt(addr)).sync();
      }


      channelFuture.channel().writeAndFlush(request).sync();
      channelFuture.channel().closeFuture().sync();

    } catch (Exception e) {
      e.printStackTrace();

    } finally {
      loopGroup.shutdownGracefully();
    }
    return rpcResponse == null ? new RpcResponse() : rpcResponse;
  }
  public static String remoteConfigStart(String request) {

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
                                  .addLast(new RpcEncoder(String.class))
                                  /*将RPC响应进行解码（返回响应）*/
                                  .addLast(new RpcDecoder(String.class))
                                  /*使用NettyClientHandler发送RPC请求*/
                                  .addLast(new NettyClientRemoteConfigHandler());

                          // 加入自己的处理器
                        }
                      });
      System.out.println("config客户端ok...");
      ChannelFuture channelFuture;
        // 启动客户端连接服务器端
      channelFuture = bootstrap.connect("127.0.0.1", 6366).sync();

      channelFuture.channel().writeAndFlush(request).sync();
      channelFuture.channel().closeFuture().sync();

    } catch (Exception e) {
      e.printStackTrace();

    } finally {
      loopGroup.shutdownGracefully();
    }
    return respConfig == null ? "--" : respConfig;
  }


}
