package com.sjl.rpc.context.netty.client;

import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import com.sjl.rpc.context.netty.abs.BaseClientTransporter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: jianlei
 * @date: 2019/12/1
 * @description: NettyClientHandler
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RocketResponse> {

    /**
     * 客户端请求的心跳命令
     */
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hb_request",
            CharsetUtil.UTF_8));
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    /**
     * 空闲次数
     */
    private int idle_count = 1;

    /**
     * 发送次数
     */
    private int count = 1;

    /**
     * 循环次数
     */
    private int fcount = 1;


    private BaseClientTransporter ct;

    public NettyClientHandler(BaseClientTransporter ct) {
        this.ct = ct;
    }

    public NettyClientHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RocketResponse rpcResponse) throws Exception {
        //简单赋值
        log.info("客户端收到服务器相应：{}", rpcResponse == null ? "---" : rpcResponse.toString());
        executor.execute(() -> {
            assert rpcResponse != null;
            if (!ct.offer(rpcResponse)) {
                throw new RuntimeException("rpc result add exception");
            }
        });
        count++;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("-------server active------");

        ctx.fireChannelActive();
    }

    /**
     * 关闭连接时
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("关闭连接时：" + new Date());
    }

    /**
     * 心跳请求处理
     * 每4秒发送一次心跳请求;
     *
     * @see IdleStateHandler.WriterIdleTimeoutTask#run()
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        System.out.println("循环请求的时间：" + new Date() + "，次数" + fcount);
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.WRITER_IDLE.equals(event.state())) {  //如果写通道处于空闲状态,就发送心跳命令
                if (idle_count <= 3) {   //设置发送次数
                    idle_count++;
                    RocketRequest request = new RocketRequest();
                    request.setRequestId("客户端心跳包->" + count);
                    ctx.channel().writeAndFlush(request);
                } else {
                    System.out.println("不再发送心跳请求了！");
                }
                fcount++;
            }
        }
    }
}
