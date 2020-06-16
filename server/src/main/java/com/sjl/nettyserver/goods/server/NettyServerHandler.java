package com.sjl.nettyserver.goods.server;

import api.mode.RpcRequest;
import api.mode.RpcResponse;
import com.google.gson.Gson;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: jianlei
 * @date: 2019/11/30
 * @description: NettyServerHandler
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
  private final ConcurrentHashMap<String, Object> handlerMap;

  public NettyServerHandler(ConcurrentHashMap<String, Object> handlerMap) {
    this.handlerMap = handlerMap;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {

    /*构造RPC响应对象*/
    RpcResponse response = new RpcResponse();
    /*设置响应ID，也就是上面的请求ID*/
    response.setReponseId(request.getRequestId());

    try {
      /*处理RPC请求*/
      Object result = handle(request);
      /*设置响应结果*/
      log.info("服务端获取相应结果:{}", result == null ? "-_-" : result);
      response.setResult(result);
    } catch (Exception e) {
      response.setError(e);
    }

    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }

  /**
   * 处理RPC请求
   *
   * @param request
   * @return
   * @throws InvocationTargetException
   */
  private Object handle(RpcRequest request)
      throws InvocationTargetException, ClassNotFoundException {
    log.info("handle 入参为:{}", new Gson().toJson(request.getClassName()));
    String className = request.getClassName();
    Object serviceBean = handlerMap.get(className);

    // 获取反射所需要的参数
    Class<?> serviceClass = serviceBean.getClass();
    String methodName = request.getMethodName();
    Class<?>[] parameterTypes = request.getParameterTypes();
    Object[] parameters = request.getParameters();

    //    cglib反射，可以改善java原生的反射性能
    FastClass serviceFastClass = FastClass.create(serviceClass);
    FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
    return serviceFastMethod.invoke(serviceBean, parameters);
  }
}
