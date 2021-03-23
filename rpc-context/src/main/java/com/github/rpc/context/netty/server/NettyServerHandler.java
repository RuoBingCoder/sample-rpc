package com.github.rpc.context.netty.server;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.filter.DelegateHandle;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: jianlei
 * @date: 2019/11/30
 * @description: NettyServerHandler
 * public interface Promise<V> extends Future<V>
 *     <code>
 *          @Override
 *     public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener) {
 *         checkNotNull(listener, "listener");
 *
 *         synchronized (this) {
 *             addListener0(listener); //添加监听器
 *         }
 *
 *         if (isDone()) {  //任务执行完了 ,另起线程异步通知监听器
 *             notifyListeners();
 *         }
 *
 *         return this;
 *     }
 *     </code>
 *
 *     SimpleChannelInboundHandler 处理完消息手动释放消息
 *     ChannelInboundHandlerAdapter 处理完消息不会手动释放消息 需要ReferenceCountUtil.release(request)
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RocketRequest> {
    private final ConcurrentHashMap<String, Object> handlerMap;

    /**
     * 空闲次数
     */
    private int idle_count = 1;
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
        /*构造RPC响应对象*/
        RocketResponse response = new RocketResponse();
        /*设置响应ID，也就是上面的请求ID*/
        if (isHeartPack(request)) {
            ctx.fireChannelRead(request);
            return;
        }
        response.setResponseId(request.getRequestId());
        //附加参数打印
        final Map<String, String> rpcAttachments = request.getRpcAttachments();
        if (CollectionUtil.isNotEmpty(rpcAttachments)) {
            rpcAttachments.forEach((k, v) -> {
                log.info("Attachment key is:{} value is:{}", k, JSONObject.toJSONString(v));
            });
        }

        try {
            /*处理RPC请求*/
            handle(request, response);
            /*设置响应结果*/
            log.info("服务端获取相应结果:{}", response.getResult() == null ? "-_-" : response.getResult());
//            response.setResult(result);
        } catch (Exception e) {
            response.setError(e);
        }
        count++;
        ctx.writeAndFlush(response);
        //SimpleChannelInboundHandler 处5理完消息手动释放消息
    }

    private boolean isHeartPack(RocketRequest request) {
        return StringUtils.isNotBlank(request.getHeartPackMsg()) && StringUtils.isBlank(request.getRequestId());
    }

    /**
     * 处理RPC请求
     *
     * @param request
     * @param response
     * @return
     * @throws InvocationTargetException
     */
    private Object handle(RocketRequest request, RocketResponse response)
            throws InvocationTargetException, ClassNotFoundException {
        log.info("handle 入参为:className:{} methodName:{} ", request.getClassName(), request.getMethodName());
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);

        // 获取反射所需要的参数
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        //    cglib反射，可以改善java原生的反射性能
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        final DelegateHandle delegateHandle = new DelegateHandle();
        final RocketResponse res = delegateHandle.invoke(response, serviceFastMethod, serviceBean, parameters);
        if (res == null) {
            return serviceFastMethod.invoke(serviceBean, parameters);
        }
        return res;

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.READER_IDLE.equals(event.state())) {  //如果读通道处于空闲状态，说明没有接收到心跳命令
                log.info("已经 5 秒没有接收到客户端的信息了");
                if (idle_count > 2) {
                    log.info("关闭这个不活跃的channel");
                    ctx.channel().close();
                }
                idle_count++;
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
