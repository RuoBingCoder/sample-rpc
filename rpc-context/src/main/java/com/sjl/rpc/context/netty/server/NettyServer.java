package com.sjl.rpc.context.netty.server;

import com.sjl.rpc.context.annotation.SjlRpcService;
import com.sjl.rpc.context.codec.RpcDecoder;
import com.sjl.rpc.context.codec.RpcEncoder;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.mode.RpcResponse;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.zk.provider.zk.ZkPublish;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * @author: jianlei
 * @date: 2019/11/30
 * @description: NettyServer
 */
@Component
@DependsOn("springBeanUtil")
public class NettyServer {


  public  static void start() {

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workGroup = new NioEventLoopGroup(); // 默认cpu核心数的2倍

    try {


      String bindAddr=InetAddress.getLocalHost().getHostAddress();
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
                  socketChannel.pipeline()
                          //将RPC请求进行解码（为了处理请求）
                          .addLast(new RpcDecoder(RpcRequest.class))
                          //将RPC请求进行编码（为了返回响应）
                          .addLast(new RpcEncoder(RpcResponse.class))
                          //处理RPC请求
                          .addLast(new NettyServerHandler(SpringBeanUtil.getBeansByAnnotation(SjlRpcService.class),bindAddr));
                }
              });
        //TODO 注册中心待开发
      ChannelFuture future = serverBootstrap.bind(bindAddr,8848).sync();

      // 创建一个监听器 异步处理不会阻塞
      future.addListener(
          (ChannelFutureListener)
              channelFuture -> {
                if (channelFuture.isSuccess()) {
                  System.out.println("监听端口是：8848");

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
