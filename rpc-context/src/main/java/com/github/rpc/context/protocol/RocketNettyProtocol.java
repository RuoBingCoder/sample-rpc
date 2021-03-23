package com.github.rpc.context.protocol;

import com.github.rpc.context.netty.client.NettyClient;

/**
 * @author jianlei.shi
 * @date 2021/1/10 2:26 下午
 * @description RocketProtocol
 */

public class RocketNettyProtocol implements Protocol<NettyClient>{

    @Override
    public NettyClient export() {
        return new NettyClient();
    }
}
