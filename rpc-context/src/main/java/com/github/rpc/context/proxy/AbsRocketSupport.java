package com.github.rpc.context.proxy;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.exception.RocketException;
import com.github.rpc.context.netty.client.NettyClient;
import com.github.rpc.context.netty.channel.NettyChannel;
import com.github.rpc.context.protocol.ProtocolFactory;
import com.github.rpc.context.protocol.RocketHttpProtocol;
import com.github.rpc.context.protocol.RocketNettyProtocol;
import com.github.rpc.context.remote.discover.RocketServiceDiscover;
import com.github.rpc.context.spring.annotation.RocketReferenceAttribute;
import com.github.rpc.context.util.RocketContext;
import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.util.SpringBeanUtil;
import com.github.rpc.context.util.StringUtils;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

/**
 * @author jianlei.shi
 * @date 2021/3/15 3:32 下午
 * @description AbsRocketSuuport
 */
@Slf4j
public abstract class AbsRocketSupport implements InvocationHandler {


    protected Object doInvoke(RocketReferenceAttribute referenceAttribute, Method method, Object[] args) {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        RocketResponse response = null;

        try {
            RocketRequest request = getRequest(method, args, referenceAttribute);
            Object protocol = ProtocolFactory.getProtocol(referenceAttribute.getProtocol());
            if (protocol instanceof RocketNettyProtocol) {
                int retries = 0;
                while (retries < 3) {
                    doHandleTask(protocol, request);
                    response = NettyChannel.getAndRemoveResp(request.getRequestId());
                    log.info("-->>>>>>>>response result is:{}", response.getResult());
                    if (response.getError() != null) {
                        retries++;
                    } else {
                        return response.getResult();
                    }
                }
                return response.getResult();
            } else if (protocol instanceof RocketHttpProtocol) {
                //todo
                return null;

            }
        } catch (Exception e) {
            log.error("远程调用服务类:[" + className + "]" + "-方法[" + methodName + "]异常!异常信息是:", e);
            throw new RocketException("远程调用服务类:[" + className + "]" + "-方法[" + methodName + "]异常!");
        }
        return null;
    }


    private void doHandleTask(Object protocol, RocketRequest request) {
        RocketNettyProtocol nettyProtocol = (RocketNettyProtocol) protocol;
        String[] host = select(request);
        NettyClient nettyClient = nettyProtocol.export();
//        final ExecutorService executor = TaskThreadPoolHelper.getExecutor();
        setAttachments(request);
        log.info("#########client get  Attachment is:{}", JSONObject.toJSONString(request.getRpcAttachments() == null ? "a" : request.getRpcAttachments()));
//        executor.execute(()->{
        log.info("netty connect thread info:{}", Thread.currentThread().getName());
        nettyClient.connect(host);
        final Channel channel = nettyClient.getChannel();
        try {
            //先放入缓存,初始化map,以便于后期阻塞队列获取值
            NettyChannel.send(channel, request);
        } catch (Exception e) {
            log.error("write response exception!", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("remote request params is:{} ", JSONObject.toJSONString(request));
        }
    }

    private String[] select(RocketRequest request) {
        return getProviderHost(request);
    }

    protected String[] getProviderHost(RocketRequest request) {
        RocketServiceDiscover rpcServiceDiscover = SpringBeanUtil.getBean(RocketServiceDiscover.class);
        return split(request, rpcServiceDiscover);
    }

    private String[] split(RocketRequest request, RocketServiceDiscover rpcServiceDiscover) {
        //loadbalancer
        final String rpcMeta = rpcServiceDiscover.selectService(request);
        return StringUtils.split(rpcMeta);
    }


    private void setAttachments(RocketRequest request) {
        final Map<String, String> attachments = RocketContext.getContext().getAttachments();
        if (CollectionUtil.isNotEmpty(attachments)) {
            request.setAttachments(attachments);
        }
    }

    private Object doHandleHttp(RocketRequest request, Object protocol) {
        return null;
    }

    private RocketRequest getRequest(Method method, Object[] args, RocketReferenceAttribute referenceAttribute) {
        RocketRequest request = new RocketRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        request.setVersion(referenceAttribute.getVersion());
        request.setTimeout(referenceAttribute.getTimeout());
        return request;

    }
}
