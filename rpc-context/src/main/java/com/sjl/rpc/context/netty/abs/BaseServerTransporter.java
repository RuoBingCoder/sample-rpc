package com.sjl.rpc.context.netty.abs;

import com.sjl.rpc.context.netty.server.ServerInit;
import com.sjl.rpc.context.spring.annotation.RocketService;
import com.sjl.rpc.context.codec.RocketDecoder;
import com.sjl.rpc.context.codec.RocketEncoder;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import com.sjl.rpc.context.netty.server.NettyServerHandler;
import com.sjl.rpc.context.netty.service.Transporter;
import com.sjl.rpc.context.util.SpringBeanUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author: JianLei
 * @date: 2020/9/11 3:13 下午
 * @description:
 */
@Slf4j
public abstract class BaseServerTransporter implements Transporter {
    protected final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    protected final EventLoopGroup workGroup = new NioEventLoopGroup(); // 默认cpu核心数的2倍

    @Override
    public void bind() {
        try {
            String bindAddr = InetAddress.getLocalHost().getHostAddress();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup, workGroup) // 设置两个线程组
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置队列连接数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持连接活动状态
                    .childHandler(new ServerInit());
            ChannelFuture future = serverBootstrap.bind(bindAddr, Constant.PORT).sync();
            doListener(future, bindAddr, serverBootstrap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            bossGroup.shutdownGracefully(); // 优雅的关闭
        }
    }

    protected void doListener(ChannelFuture future, String bindAddr, ServerBootstrap serverBootstrap) throws InterruptedException {
        // 创建一个监听器 异步处理不会阻塞
        future.addListener(
                (ChannelFutureListener)
                        channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                log.info("监听端口是：{}", Constant.PORT);
                            } else {
                                log.error("监听执行失败开始重试");
                                future.channel().eventLoop().schedule(() -> {
                                    try {
                                        serverBootstrap.bind(bindAddr, Constant.PORT).sync();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }, 1L, TimeUnit.SECONDS);
                            }
                        });
        future.channel().closeFuture().sync(); //阻塞主线程
    }

}
