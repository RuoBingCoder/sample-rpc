package com.sjl.rpc.context.proxy;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.netty.helper.TaskThreadPoolHelper;
import com.sjl.rpc.context.spring.annotation.RocketReferenceAttribute;
import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import com.sjl.rpc.context.exception.RocketException;
import com.sjl.rpc.context.netty.client.NettyClient;
import com.sjl.rpc.context.protocol.ProtocolFactory;
import com.sjl.rpc.context.protocol.RocketHttpProtocol;
import com.sjl.rpc.context.protocol.RocketNettyProtocol;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.*;

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
        String version = referenceAttribute.getVersion();
        try {
            RocketRequest request = getRequest(method, args, referenceAttribute);
            Object protocol = ProtocolFactory.getProtocol(referenceAttribute.getProtocol());
            if (protocol instanceof RocketNettyProtocol) {
                final NettyClient nettyClient = doHandleTcp(protocol, request);
                final RocketResponse response =nettyClient.take();
                log.info("-->>>>>>>>response result is:{}", response.getResult());
                return response.getResult();
            } else if (protocol instanceof RocketHttpProtocol) {

                return null;

            }
        } catch (Exception e) {
            log.error("远程调用服务类:[" + className + "]" + "-方法[" + methodName + "]异常!异常信息是:", e);
            throw new RocketException("远程调用服务类:[" + className + "]" + "-方法[" + methodName + "]异常!");
        }
        return null;
    }

    private NettyClient doHandleTcp(Object protocol, RocketRequest request) {
        RocketNettyProtocol nettyProtocol = (RocketNettyProtocol) protocol;
        NettyClient nettyClient = nettyProtocol.export();
        final ExecutorService executor = TaskThreadPoolHelper.getExecutor();
        executor.execute(()->{
            log.info("netty connect thread info:{}",Thread.currentThread().getName());
            nettyClient.connect(request,nettyClient);
            if (log.isDebugEnabled()) {
                log.debug("remote request params is:{} ", JSONObject.toJSONString(request));
            }
        });
        TaskThreadPoolHelper.addExecutor(executor); //GC thread pool
        return nettyClient;
    }

    private Object doHandleHttp(RocketRequest request, Object protocol) {


        return null;
    }

    private RocketRequest getRequest(Method method, Object[] args, RocketReferenceAttribute referenceAttribute){
            RocketRequest request = new RocketRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setClassName(method.getDeclaringClass().getName());
            request.setMethodName(method.getName());
            request.setParameters(args);
            request.setParameterTypes(method.getParameterTypes());
            request.setVersion(referenceAttribute.getVersion());
            return request;

        }
    }
