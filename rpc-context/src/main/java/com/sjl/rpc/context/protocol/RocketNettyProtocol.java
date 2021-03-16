package com.sjl.rpc.context.protocol;

import com.sjl.rpc.context.netty.client.NettyClient;
import com.sjl.rpc.context.util.SpringBeanUtil;

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
