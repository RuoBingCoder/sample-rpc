package com.sjl.rpc.context.remote.registry;

import com.sjl.rpc.context.annotation.RpcService;
import com.sjl.rpc.context.exception.RpcException;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.remote.client.CuratorClient;
import com.sjl.rpc.context.remote.handle.abs.BaseRpcHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: JianLei
 * @date: 2020/9/6 2:28 下午
 * @description: zk服务注册
 */
@Slf4j
@Component
@DependsOn("springBeanUtil")
public class ZkServiceRegistry extends BaseRpcHandler
    implements IServiceRegistry, InitializingBean, DisposableBean {
  @Override
  public void registry(Class<?> serviceName, String version) throws Exception {
    // 预留接口
    if (serviceName != null) {
      boolean flag = doCheckServiceNodeIsExists(serviceName.getName(), version);
      if (flag) {
        createServicePath(
            CuratorClient.instance(), handleCacheMapServiceName(serviceName.getName(), version));
      }
      return;
    }
    final ConcurrentHashMap<String, Object> beans =
        SpringBeanUtil.getBeansByAnnotation(RpcService.class);
    beans.forEach(
        (beanName, bean) -> {
          try {
            final RpcService sjlRpcService = bean.getClass().getAnnotation(RpcService.class);
            final String v = sjlRpcService.version();
            boolean flag = doCheckServiceNodeIsExists(beanName, v);
            if (flag) {
              createServicePath(CuratorClient.instance(), handleCacheMapServiceName(beanName, v));
            }
          } catch (Exception e) {
            log.error("====>发布服务出现异常", e);
            throw new RpcException("发布服务出现异常");
          }
        });
  }

  @Override
  public void destroy() {
    log.info("==============开始销毁缓存map,curator关闭!================");
    cacheServiceMap.clear();
    CuratorClient.instance().close();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    registry(null, null);
  }
}
