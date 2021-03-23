package com.github.rpc.context.netty.client;

import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.netty.abs.BaseClientTransporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: jianlei
 * @date: 2019/12/1
 * @description: NettyClient
 */
@Component
@Slf4j
public class NettyClient extends BaseClientTransporter {

    public static NettyClient instance() {
        return new NettyClient();
    }

    public static void start(RocketRequest request) {

    }

}
