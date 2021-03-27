package com.github.rpc.context.netty.handle;

import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.netty.transport.BaseClientTransporter;
import com.github.rpc.context.netty.channel.NettyChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final AtomicInteger heatbeatReceiveCount = new AtomicInteger(4);

    private final AtomicBoolean isEx = new AtomicBoolean(false);

    public NettyClientHandler(BaseClientTransporter ct) {
        this.ct = ct;
    }

    public NettyClientHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RocketResponse rpcResponse) throws Exception {
        //简单赋值
        log.info("客户端收到服务器相应：{}", rpcResponse == null ? "---" : rpcResponse.toString());
        assert rpcResponse != null;
        final NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), rpcResponse.getResponseId());
        if (rpcResponse.getIsHeartPack()) {
            heatbeatReceiveCount.incrementAndGet();
        }
        channel.receive(rpcResponse);
       /* executor.execute(() -> {
            assert rpcResponse != null;
            if (!ct.offer(rpcResponse)) {
                throw new RuntimeException("rpc result add exception");
            }
        });*/

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
        final NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), null);
        channel.removeChannel(ctx.channel());

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
                    request.setHeartPackMsg("客户端心跳包->" + count);
                    ctx.channel().writeAndFlush(request);
                } else {
                    System.out.println("不再发送心跳请求了！");
                    inactive(ctx);

                }
                fcount++;
            }
        }
    }

    private void inactive(ChannelHandlerContext ctx) {
        CompletableFuture.runAsync(() -> {
            while (!isEx.get()) {
                try {
                    Thread.sleep(1000);
                    if (heatbeatReceiveCount.get() == 4) {
                        ctx.fireChannelInactive();
                        heatbeatReceiveCount.set(0);
                    }
                } catch (Exception e) {
                    isEx.set(true);
                }
            }

        });
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }
}
