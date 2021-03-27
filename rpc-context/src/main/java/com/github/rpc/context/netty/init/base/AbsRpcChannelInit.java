package com.github.rpc.context.netty.init.base;

import cn.hutool.core.collection.CollectionUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.net.UnknownHostException;
import java.util.List;

/**
 * @author jianlei.shi
 * @date 2021/3/16 11:00 上午
 * @description
 */

public abstract class AbsRpcChannelInit extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch
                .pipeline();
        final List<ChannelHandler> handlers = getHandlers();
        if (CollectionUtil.isNotEmpty(handlers)){
            for (ChannelHandler handler : handlers) {
                pipeline.addLast(handler);
            }
        }

    }

    protected abstract List<ChannelHandler>  getHandlers() throws UnknownHostException;


}
