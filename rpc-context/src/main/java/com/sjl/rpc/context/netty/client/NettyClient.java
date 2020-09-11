package com.sjl.rpc.context.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.codec.RpcDecoder;
import com.sjl.rpc.context.codec.RpcEncoder;
import com.sjl.rpc.context.exception.RpcException;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.mode.RpcResponse;
import com.sjl.rpc.context.netty.abs.BaseClientTransporter;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.remote.discover.RpcServiceDiscover;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author: jianlei
 * @date: 2019/12/1
 * @description: NettyClient
 */
@Component
@Slf4j
public class NettyClient extends BaseClientTransporter {


  public static RpcResponse start(RpcRequest request) {
    return new NettyClient().connect(request);
  }

  @Override
  public void bind() {
  }
}
