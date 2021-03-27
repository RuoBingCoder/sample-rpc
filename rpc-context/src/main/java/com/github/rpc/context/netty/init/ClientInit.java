package com.github.rpc.context.netty.init;

import com.github.rpc.context.netty.handle.NettyClientHandler;
import com.github.rpc.context.netty.init.base.AbsRpcChannelInit;
import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.codec.RocketDecoder;
import com.github.rpc.context.codec.RocketEncoder;
import com.github.rpc.context.netty.transport.BaseClientTransporter;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author jianlei.shi
 * @date 2021/3/16 11:07 上午
 * @description CilentInit
 */

public class ClientInit extends AbsRpcChannelInit {

    private BaseClientTransporter ct;

    public ClientInit(BaseClientTransporter ct) {
        this.ct = ct;
    }

    public ClientInit() {
    }

    @Override
    protected List<ChannelHandler> getHandlers() {
        List<ChannelHandler> handlers = new ArrayList<>();
       /* pipeline
                .addLast(new IdleStateHandler(0, 1, 0, TimeUnit.SECONDS)) //心跳包检测
                *//*将RPC请求进行编码（发送请求）*//*
                .addLast(new RocketEncoder(RocketRequest.class))
                *//*将RPC响应进行解码（返回响应）*//*
                .addLast(new RocketDecoder(RocketResponse.class))
                *//*使用NettyClientHandler发送RPC请求*//*
                .addLast(new NettyClientHandler());*/
        handlers.add(new RocketEncoder(RocketRequest.class));
        handlers.add(new RocketDecoder(RocketResponse.class));
        handlers.add(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
        handlers.add(new NettyClientHandler(ct));
        return handlers;
    }
}
