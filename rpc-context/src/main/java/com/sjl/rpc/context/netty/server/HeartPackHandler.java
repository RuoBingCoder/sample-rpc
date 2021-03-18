package com.sjl.rpc.context.netty.server;

import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jianlei.shi
 * @date 2021/3/16 11:27 上午
 * @description HeartPackHandler
 */
@Slf4j
public class HeartPackHandler extends SimpleChannelInboundHandler<RocketRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RocketRequest msg) throws Exception {
        log.info("---->>>>>>>服务端接收到💗包内容是:{}", msg.getHeartPackMsg());
        RocketResponse response=new RocketResponse();
        response.setResponseId(msg.getRequestId());
        response.setResult("this is heart pack");
        ctx.writeAndFlush(response);
    }
}
