package com.sjl.rpc.context.netty.service;

import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.mode.RpcResponse;
import io.netty.channel.ChannelFuture;

import java.lang.reflect.Method;

/**
 * @author: JianLei
 * @date: 2020/9/11 1:49 下午
 * @description:
 */

public interface Transporter {


    void bind();


    RpcResponse connect(RpcRequest request);
}
