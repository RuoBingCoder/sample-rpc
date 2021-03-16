package com.sjl.rpc.context.netty.abs;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.exception.RocketException;
import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import com.sjl.rpc.context.netty.client.ClientInit;
import com.sjl.rpc.context.netty.helper.TaskThreadPoolHelper;
import com.sjl.rpc.context.netty.service.Transporter;
import com.sjl.rpc.context.remote.discover.RocketServiceDiscover;
import com.sjl.rpc.context.util.SpringBeanUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: JianLei
 * @date: 2020/9/11 2:10 下午
 * @description:
 */
@Slf4j
public abstract class BaseClientTransporter implements Transporter {
    protected RocketResponse rpcResponse;
    protected EventLoopGroup loopGroup = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    protected LinkedBlockingQueue<RocketResponse> responseQue = new LinkedBlockingQueue<>(10);

    @Override
    public void connect(RocketRequest request, BaseClientTransporter ct) {
        try {

            bootstrap
                    .group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInit(ct));

            String[] providerHost = getProviderHost(request);
            log.info("获取远程服务地址是:{}", JSONObject.toJSONString(providerHost));
            ChannelFuture channelFuture =
                    bootstrap.connect(providerHost[0], Integer.parseInt(providerHost[1])).sync();
            doListener(channelFuture, request, ct);
        } catch (Exception e) {
            log.error("客户端连接异常", e);
        }
    }


    protected String[] getProviderHost(RocketRequest request) {
        RocketServiceDiscover rpcServiceDiscover = SpringBeanUtil.getBean(RocketServiceDiscover.class);
        return rpcServiceDiscover.selectService(request).split(":");
    }

    protected void doListener(ChannelFuture channelFuture, RocketRequest request, BaseClientTransporter ct) {
        try {

            channelFuture.addListener(
                    future -> {
                        if (!channelFuture.isSuccess()) {
                            EventLoop loop = channelFuture.channel().eventLoop();
                            loop.schedule(
                                    () -> {
                                        connect(request, ct);
                                    },
                                    1L,
                                    TimeUnit.SECONDS);
                        }
                    });
            channelFuture.channel().writeAndFlush(request).sync();
            channelFuture.channel().closeFuture().sync(); //阻塞主线程
            log.info("客户端连接远程服务成功!");
        } catch (Exception e) {
            throw new RocketException("客户端远程连接异常");

        } finally {
            loopGroup.shutdownGracefully();
            TaskThreadPoolHelper.signalStartClear(); //clear netty rpc thread
        }
    }

    public RocketResponse getRpcResponse() {
        return rpcResponse == null ? new RocketResponse() : rpcResponse;
    }

    public void setRpcResponse(RocketResponse rocketResponse) {
        this.rpcResponse = rocketResponse;
    }

    public boolean offer(RocketResponse rocketResponse) {
        return this.responseQue.offer(rocketResponse);
    }

    public RocketResponse take() throws InterruptedException {
        return this.responseQue.take();
    }
}
