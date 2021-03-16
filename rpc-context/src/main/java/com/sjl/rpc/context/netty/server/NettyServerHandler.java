package com.sjl.rpc.context.netty.server;

import com.google.gson.Gson;
import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: jianlei
 * @date: 2019/11/30
 * @description: NettyServerHandler
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

        /*构造RPC响应对象*/
        RocketResponse response = new RocketResponse();
        /*设置响应ID，也就是上面的请求ID*/
        response.setReponseId(request.getRequestId());
        if (isHeartPack(request)) {
            ctx.fireChannelRead(request);
            return;
        }

        try {
            /*处理RPC请求*/
            Object result = handle(request);
            /*设置响应结果*/
            log.info("服务端获取相应结果:{}", result == null ? "-_-" : result);
            response.setResult(result);
        } catch (Exception e) {
            response.setError(e);
        }
        count++;
        ctx.writeAndFlush(response);
    }

    private boolean isHeartPack(RocketRequest request) {
        return request.getRequestId() != null && request.getClassName() == null && request.getMethodName() == null && request.getParameters() == null && request.getParameterTypes() == null && request.getVersion() == null;
    }

    /**
     * 处理RPC请求
     *
     * @param request
     * @return
     * @throws InvocationTargetException
     */
    private Object handle(RocketRequest request)
            throws InvocationTargetException, ClassNotFoundException {
        log.info("handle 入参为:{}", new Gson().toJson(request.getClassName()));
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
        return serviceFastMethod.invoke(serviceBean, parameters);
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
