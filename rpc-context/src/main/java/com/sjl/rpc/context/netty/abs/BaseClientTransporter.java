package com.sjl.rpc.context.netty.abs;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.codec.RpcDecoder;
import com.sjl.rpc.context.codec.RpcEncoder;
import com.sjl.rpc.context.exception.RpcException;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.mode.RpcResponse;
import com.sjl.rpc.context.netty.client.NettyClientHandler;
import com.sjl.rpc.context.netty.service.Transporter;
import com.sjl.rpc.context.remote.discover.RpcServiceDiscover;
import com.sjl.rpc.context.util.SpringBeanUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author: JianLei
 * @date: 2020/9/11 2:10 下午
 * @description:
 */
@Slf4j
public abstract class BaseClientTransporter implements Transporter {
  public static RpcResponse rpcResponse;
  protected EventLoopGroup loopGroup = new NioEventLoopGroup();
  private final Bootstrap bootstrap=new Bootstrap();

  @Override
  public RpcResponse connect(RpcRequest request) {
    try {
            bootstrap
                .group(loopGroup)
                .channel(NioSocketChannel.class)
                .handler(
                    new ChannelInitializer<SocketChannel>() {
                      @Override
                      protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel
                            .pipeline()
                            /*将RPC请求进行编码（发送请求）*/
                            .addLast(new RpcEncoder(RpcRequest.class))
                            /*将RPC响应进行解码（返回响应）*/
                            .addLast(new RpcDecoder(RpcResponse.class))
                            /*使用NettyClientHandler发送RPC请求*/
                            .addLast(new NettyClientHandler());
                      }
                    });

          String[] providerHost = getProviderHost(request);
          log.info("获取远程服务地址是:{}", JSONObject.toJSONString(providerHost));
          ChannelFuture channelFuture =
              bootstrap.connect(providerHost[0], Integer.parseInt(providerHost[1])).sync();
          doListener(channelFuture, request);
    }catch (Exception e){
      log.error("客户端连接异常",e);
    }
    return rpcResponse == null ? new RpcResponse() : rpcResponse;
  }


  protected String[] getProviderHost(RpcRequest request) {
    RpcServiceDiscover rpcServiceDiscover = SpringBeanUtil.getBean(RpcServiceDiscover.class);
    return rpcServiceDiscover.selectService(request).split(":");
  }

  protected void doListener(ChannelFuture channelFuture, RpcRequest request) {
    try {

      channelFuture.addListener(
          future -> {
            if (!channelFuture.isSuccess()) {
              EventLoop loop = channelFuture.channel().eventLoop();
              loop.schedule(
                  () -> {
                    connect(request);
                  },
                  1L,
                  TimeUnit.SECONDS);
            }
          });
      channelFuture.channel().writeAndFlush(request).sync();
      channelFuture.channel().closeFuture().sync();
      log.info("客户端连接远程服务成功!");
    } catch (Exception e) {
      throw new RpcException("客户端远程连接异常");

    } finally {
      loopGroup.shutdownGracefully();
    }
  }
}
