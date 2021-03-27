package com.github.rpc.context.netty.transport;

import com.alibaba.fastjson.JSONObject;
import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.exception.RocketException;
import com.github.rpc.context.netty.init.ClientInit;
import com.github.rpc.context.remote.discover.RocketServiceDiscover;
import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.util.SpringBeanUtil;
import com.github.rpc.context.util.StringUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
    protected LinkedBlockingQueue<RocketResponse> responseQue = new LinkedBlockingQueue<>(1);
    protected Channel channel;

    @Override
    public void connect(String[] hosts) {
        ChannelFuture channelFuture;
        try {
            bootstrap
                    .group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInit(this));

            log.info("获取远程服务地址是:{}", JSONObject.toJSONString(hosts));
            channelFuture =
                    bootstrap.connect(getAddress(hosts)).sync();
            doListener(channelFuture, hosts);
        } catch (Exception e) {
            log.error("客户端连接异常", e);
//            loopGroup.shutdownGracefully();
        }
    }

    private SocketAddress getAddress(String[] providerHost) {
        return new InetSocketAddress(providerHost[0], Integer.parseInt(providerHost[1]));
    }



    protected void doListener(ChannelFuture channelFuture, String[] hosts) {
        try {

            channelFuture.addListener(
                    future -> {
                        if (!channelFuture.isSuccess()) {
                            EventLoop loop = channelFuture.channel().eventLoop();
                            loop.schedule(
                                    () -> {
                                        //重新连接
                                        connect(hosts);
                                    },
                                    1L,
                                    TimeUnit.SECONDS);
                        }
                    });
            this.channel = channelFuture.channel();
//            channelFuture.channel().writeAndFlush(request);
            log.info("客户端连接远程服务成功!");
//            channelFuture.channel().closeFuture().sync(); //阻塞主线程
        } catch (Exception e) {
            log.error("客户端远程连接异常: ", e);
            throw new RocketException("客户端远程连接异常");
        } finally {
//            this.channel=null;
//            loopGroup.shutdownGracefully();
//            TaskThreadPoolHelper.signalStartClear(); //clear netty rpc thread
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

    public Channel getChannel() {
        if (this.channel == null) {
            throw new RocketException("channel is closed!");
        }
        return this.channel;
    }
}
