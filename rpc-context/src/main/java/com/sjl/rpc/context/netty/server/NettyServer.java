package com.sjl.rpc.context.netty.server;

import com.sjl.rpc.context.annotation.RpcService;
import com.sjl.rpc.context.codec.RpcDecoder;
import com.sjl.rpc.context.codec.RpcEncoder;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.mode.RpcResponse;
import com.sjl.rpc.context.netty.abs.BaseServerTransporter;
import com.sjl.rpc.context.util.SpringBeanUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author: jianlei
 * @date: 2019/11/30
 * @description: NettyServer
 */
@Component
@DependsOn("springBeanUtil")
@Slf4j
public class NettyServer extends BaseServerTransporter {

  //  @PostConstruct
  public static void start() {
      new NettyServer().bind();
  }

    @Override
    public RpcResponse connect(RpcRequest request) {
        return null;
    }
}
