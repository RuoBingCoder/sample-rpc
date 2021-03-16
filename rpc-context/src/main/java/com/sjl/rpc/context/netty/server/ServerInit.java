package com.sjl.rpc.context.netty.server;

import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import com.sjl.rpc.context.codec.RocketDecoder;
import com.sjl.rpc.context.codec.RocketEncoder;
import com.sjl.rpc.context.netty.abs.AbsRpcChannelInit;
import com.sjl.rpc.context.netty.client.HeartPackHandler;
import com.sjl.rpc.context.spring.annotation.RocketService;
import com.sjl.rpc.context.util.SpringBeanUtil;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author jianlei.shi
 * @date 2021/3/16 11:09 上午
 * @description ServerInit
 */

public class ServerInit extends AbsRpcChannelInit {
    @Override
    protected List<ChannelHandler> getHandlers() throws UnknownHostException {
        String bindAddr = InetAddress.getLocalHost().getHostAddress();
        List<ChannelHandler> handlers = new ArrayList<>();
        handlers.add(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
        handlers.add(new RocketDecoder(RocketRequest.class));
        handlers.add(new RocketEncoder(RocketResponse.class));
        handlers.add(new NettyServerHandler(
                SpringBeanUtil.getBeansByAnnotation(RocketService.class), bindAddr));
        handlers.add(new HeartPackHandler());
 /*addLast(new IdleStateHandler(1, 0, 0, TimeUnit.SECONDS)) //心跳包检测
                // 将RPC请求进行解码（为了处理请求）
                .addLast(new RocketDecoder(RocketRequest.class))
                // 将RPC请求进行编码（为了返回响应）
                .addLast(new RocketEncoder(RocketResponse.class))
                // 处理RPC请求
                .addLast(
                        new NettyServerHandler(
                                SpringBeanUtil.getBeansByAnnotation(RocketService.class), bindAddr));*/
        return handlers;
    }
}
