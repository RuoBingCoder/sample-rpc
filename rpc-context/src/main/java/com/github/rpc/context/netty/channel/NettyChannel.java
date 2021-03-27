package com.github.rpc.context.netty.channel;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.exception.RocketException;
import com.github.rpc.context.filter.DelegateFilterInvoker;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author jianlei.shi
 * @date 2021/3/25 4:33 下午
 * @description: NettyChannel
 */
@Slf4j
public class NettyChannel {

    private static final Map<Channel, NettyChannel> CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final Map<String, LinkedBlockingQueue<RocketResponse>> RESPONSE_META = new ConcurrentHashMap<>();

    private boolean isAlive;

    private String id;

    private Channel channel;


    public NettyChannel(String id, Channel channel) {
        this.id = id;
        this.channel = channel;
    }

    public NettyChannel(String id) {
        this.id = id;
    }

    public static NettyChannel getOrAddChannel(Channel ch, String id) {
        if (ch == null) {
            return null;
        }
        NettyChannel ret = CHANNEL_MAP.get(ch);
        if (ret == null) {
            NettyChannel nettyChannel = new NettyChannel(id, ch);
            if (ch.isActive()) {
                nettyChannel.markActive(true);
                ret = CHANNEL_MAP.putIfAbsent(ch, nettyChannel);
            }
            if (ret == null) {
                ret = nettyChannel;
            }
        }
        return ret;
    }

    private void markActive(boolean isAlive) {
        this.isAlive = isAlive;
    }


    public void receive(RocketResponse response) {
        if (!response.getIsHeartPack()) {
            final LinkedBlockingQueue<RocketResponse> queue = RESPONSE_META.get(response.getResponseId());
            if (!queue.offer(response)) {
                throw new RocketException("queue offer exception");
            }
        }
    }

    public void removeChannel(Channel channel) {
        CHANNEL_MAP.remove(channel);
        channel.close();
        log.info("CHANNEL_MAP size:{}", CHANNEL_MAP.size());
    }


    public static RocketResponse getAndRemoveResp(String id) {
        try {
            RocketResponse response = RESPONSE_META.get(id).take();
            RESPONSE_META.remove(id);
            return response;
        } catch (Exception e) {
            throw new RocketException("getAndRemoveResp exception");
        }
    }

    public static void putReqId(String id) {
        try {
            RESPONSE_META.put(id, new LinkedBlockingQueue<>(1));
        } catch (Exception e) {
            throw new RocketException("putReqId exception");
        }
    }

    public void reply(RocketRequest request, ConcurrentHashMap<String, Object> services) {
        //多个IO请求会阻塞,所以要异步
        doAsyncHandle(this.channel, request, services);
        //SimpleChannelInboundHandler 处5理完消息手动释放消息
    }

    private void doAsyncHandle(Channel channel, RocketRequest request, ConcurrentHashMap<String, Object> handlerMap) {
        //ForkJoinPool by default
        CompletableFuture.runAsync(() -> {
            /*构造RPC响应对象*/
            RocketResponse response = new RocketResponse();
            response.setResponseId(request.getRequestId());
            response.setIsHeartPack(false);
            //附加参数打印
            if (log.isDebugEnabled()) {
                final Map<String, String> rpcAttachments = request.getRpcAttachments();
                if (CollectionUtil.isNotEmpty(rpcAttachments)) {
                    rpcAttachments.forEach((k, v) -> {
                        log.debug("Attachment key is:{} value is:{}", k, JSONObject.toJSONString(v));
                    });
                }
            }

            try {
                /*处理RPC请求*/
                handle(request, response, handlerMap);
                /*设置响应结果*/
                log.info("服务端获取相应结果:{}", response.getResult() == null ? "-_-" : response.getResult());
            } catch (Exception e) {
                log.error("server invoke exception msg:", e);
                response.setError(e);
            }
            channel.writeAndFlush(response);
        });

    }

    /**
     * 处理RPC请求
     *
     * @param request
     * @param response
     * @param handlerMap
     */
    private void handle(RocketRequest request, RocketResponse response, ConcurrentHashMap<String, Object> handlerMap) {
        log.info("handle 入参为:className:{} methodName:{} ", request.getClassName(), request.getMethodName());
        try {
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
            //处理过滤器
            final DelegateFilterInvoker filterInvoker = new DelegateFilterInvoker();
            final RocketResponse res = filterInvoker.invoke(response, serviceFastMethod, serviceBean, parameters);
            if (res == null) {
                serviceFastMethod.invoke(serviceBean, parameters);
            }
        } catch (Exception e) {
            log.error("channel handle request exception msg:", e);
            throw new RocketException("channel handle request exception");
        }

    }


    public static void send(Channel channel, RocketRequest request) {
        try {
            putReqId(request.getRequestId());
            channel.writeAndFlush(request).sync();
        } catch (Exception e) {

        }

    }

}
