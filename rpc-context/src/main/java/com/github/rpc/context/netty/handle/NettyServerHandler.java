package com.github.rpc.context.netty.handle;

import com.alibaba.fastjson.JSONObject;
import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.netty.channel.NettyChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: jianlei
 * @date: 2019/11/30
 * @description: NettyServerHandler
 * public interface Promise<V> extends Future<V>
 * <code>
 * @Override public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener) {
 * checkNotNull(listener, "listener");
 * <p>
 * synchronized (this) {
 * addListener0(listener); //添加监听器
 * }
 * <p>
 * if (isDone()) {  //任务执行完了 ,另起线程异步通知监听器
 * notifyListeners();
 * }
 * <p>
 * return this;
 * }
 * </code>
 * <p>
 * SimpleChannelInboundHandler 处理完消息手动释放消息
 * ChannelInboundHandlerAdapter 处理完消息不会手动释放消息 需要ReferenceCountUtil.release(request)
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RocketRequest> {
    private final ConcurrentHashMap<String, Object> handlerMap;

    /**
     * 空闲次数
     */
    private final AtomicInteger idle_count = new AtomicInteger(1);
    /**
     * 发送次数
     */
    private int count = 1;
    //注册到zk服务
    private String host;

    public NettyServerHandler(ConcurrentHashMap<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    public NettyServerHandler(ConcurrentHashMap<String, Object> handlerMap, String host) {
        this.handlerMap = handlerMap;
        this.host = host;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RocketRequest request) throws Exception {
        log.info("request params:{}", JSONObject.toJSONString(request));
        if (isHeartPack(request)) {
            ctx.fireChannelRead(request);
            return;
        }
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), request.getRequestId());
        channel.reply(request, handlerMap);

    }

    private boolean isHeartPack(RocketRequest request) {
        return StringUtils.isNotBlank(request.getHeartPackMsg()) && StringUtils.isBlank(request.getRequestId());
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.READER_IDLE.equals(event.state())) {  //如果读通道处于空闲状态，说明没有接收到心跳命令
                log.info("已经 5 秒没有接收到客户端的信息了");
                if (idle_count.get() > 2) {
                    log.info("关闭这个不活跃的channel");
                    NettyChannel.getOrAddChannel(ctx.channel(), null).removeChannel(ctx.channel());
                    idle_count.set(1);
                    ctx.channel().close();
                }
                idle_count.incrementAndGet();
            }
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }


    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端异常", cause);
        ctx.close();
    }
}
