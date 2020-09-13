package com.sjl.rpc.context.netty.abs;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.codec.RocketDecoder;
import com.sjl.rpc.context.codec.RocketEncoder;
import com.sjl.rpc.context.exception.RocketException;
import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import com.sjl.rpc.context.netty.client.NettyClientHandler;
import com.sjl.rpc.context.netty.service.Transporter;
import com.sjl.rpc.context.remote.discover.RocketServiceDiscover;
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
  public static RocketResponse rpcResponse;
  protected EventLoopGroup loopGroup = new NioEventLoopGroup();
  private final Bootstrap bootstrap=new Bootstrap();

  @Override
  public RocketResponse connect(RocketRequest request) {
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
                            .addLast(new RocketEncoder(RocketRequest.class))
                            /*将RPC响应进行解码（返回响应）*/
                            .addLast(new RocketDecoder(RocketResponse.class))
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
    return rpcResponse == null ? new RocketResponse() : rpcResponse;
  }


  protected String[] getProviderHost(RocketRequest request) {
    RocketServiceDiscover rpcServiceDiscover = SpringBeanUtil.getBean(RocketServiceDiscover.class);
    return rpcServiceDiscover.selectService(request).split(":");
  }

  protected void doListener(ChannelFuture channelFuture, RocketRequest request) {
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
      throw new RocketException("客户端远程连接异常");

    } finally {
      loopGroup.shutdownGracefully();
    }
  }
}
