package com.sjl.nettyserver.goods.server;

import api.mode.RpcRequest;
import api.mode.RpcResponse;
import com.sjl.nettyserver.goods.annotation.RpcService;
import com.sjl.nettyserver.goods.codec.RpcDecoder;
import com.sjl.nettyserver.goods.codec.RpcEncoder;
import com.sjl.nettyserver.goods.util.SpringBeanUtil;
import com.sjl.nettyserver.goods.zk.ZkRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * @author: jianlei
 * @date: 2019/11/30
 * @description: NettyServer
 */
@Component
@DependsOn("springBeanUtil")
public class NettyServer {

  public static void rpcStart() {

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workGroup = new NioEventLoopGroup(); // 默认cpu核心数的2倍

    try {

      ServerBootstrap serverBootstrap = new ServerBootstrap();
      serverBootstrap
          .group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class) // 设置两个线程组
          .option(ChannelOption.SO_BACKLOG, 128) // 设置队列连接数
          .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持连接活动状态
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                // 给pipeline 设置处理器

                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                  socketChannel
                      .pipeline()
                      // 将RPC请求进行解码（为了处理请求）
                      .addLast(new RpcDecoder(RpcRequest.class))
                      // 将RPC请求进行编码（为了返回响应）
                      .addLast(new RpcEncoder(RpcResponse.class))
                      // 处理RPC请求
                      .addLast(
                          new NettyServerHandler(
                              SpringBeanUtil.getBeansByAnnotation(RpcService.class)));

                }
              });

      ChannelFuture future = serverBootstrap.bind(6366).sync();
      new ZkRegister("127.0.0.1:6366").register();
      // 创建一个监听器 异步处理不会阻塞
      future.addListener(
          (ChannelFutureListener)
              channelFuture -> {
                if (channelFuture.isSuccess()) {
                  System.out.println("监听端口是：6366");

                } else {
                  System.out.println("监听执行失败！");
                }
              });
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();

    } finally {

      bossGroup.shutdownGracefully(); // 优雅的关闭
    }
  }

    public static void remoteStart() {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(); // 默认cpu核心数的2倍

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class) // 设置两个线程组
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置队列连接数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持连接活动状态
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                // 给pipeline 设置处理器

                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    socketChannel
                                            .pipeline()
                                            // 将RPC请求进行解码（为了处理请求）
                                            .addLast(new RpcDecoder(String.class))
                                            // 将RPC请求进行编码（为了返回响应）
                                            .addLast(new RpcEncoder(String.class))
                                            // 处理RPC请求

                                            .addLast(new NettyServerRemoteConfigCenterHandler());
                                }
                            });

            ChannelFuture future = serverBootstrap.bind(6366).sync();
            // 创建一个监听器 异步处理不会阻塞
            future.addListener(
                    (ChannelFutureListener)
                            channelFuture -> {
                                if (channelFuture.isSuccess()) {
                                    System.out.println("监听端口是：6366");

                                } else {
                                    System.out.println("监听执行失败！");
                                }
                            });
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();

        } finally {

            bossGroup.shutdownGracefully(); // 优雅的关闭
        }
    }
}
