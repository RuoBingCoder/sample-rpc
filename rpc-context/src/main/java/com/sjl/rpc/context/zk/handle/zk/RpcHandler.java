package com.sjl.rpc.context.zk.handle.zk;

import com.sjl.rpc.context.annotation.SjlRpcService;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.exception.RpcException;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.zk.client.CuratorClient;
import com.sjl.rpc.context.zk.loadbalance.LoadBalance;
import com.sjl.rpc.context.zk.handle.abs.BaseServiceOperate;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: JianLei
 * @date: 2020/9/1 5:28 下午
 * @description:
 */
@Component
@DependsOn("springBeanUtil")
@Slf4j
public class RpcHandler extends BaseServiceOperate implements InitializingBean, DisposableBean {

  private static final Map<String, String> cacheServiceMap = new ConcurrentHashMap<>();

  @Override
  public void publishService() {
    final ConcurrentHashMap<String, Object> beans =
        SpringBeanUtil.getBeansByAnnotation(SjlRpcService.class);
    beans.forEach(
        (beanName, bean) -> {
          try {
            final SjlRpcService sjlRpcService = bean.getClass().getAnnotation(SjlRpcService.class);
            final String version = sjlRpcService.version();
            if (!"".equals(version)) {
              if (!cacheServiceMap.containsKey(handleServiceName(beanName,version))
                  || CuratorClient.instance().checkExists().forPath(handleServiceName(beanName,version)) == null) {
                createServicePath(CuratorClient.instance(), handleServiceName(beanName,version));
              } else {
                cacheServiceMap.put(
                    beanName + "&" + version, InetAddress.getLocalHost().getHostAddress());
              }
            }
          } catch (Exception e) {
            log.error("====>发布服务出现异常", e);
            throw new RpcException("发布服务出现异常");
          }
        });
  }

  private void createServicePath(CuratorFramework curator, String serviceName) throws Exception {
    String servicePath = handleZkNodePath(serviceName);
     try {
       curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath);
     } catch (Exception e) {
       log.error("====>create zk node error!error message is:",e);
       throw new RpcException("创建节点异常");


     }
  }
  private String handleZkNodePath(String serviceName) throws UnknownHostException {
    return Constant.ROOT_PATH + serviceName + "/" + InetAddress.getLocalHost().getHostAddress();
  }

  public String getChildNodeData(RpcRequest request) throws Exception {
    final CuratorFramework instance = CuratorClient.instance();
    String providerHost;
    String serviceNameSpace =
         handleServiceName(request.getClassName(), request.getVersion());
    if (cacheServiceMap.get(serviceNameSpace) != null) {
      log.info("开始走缓存获取服务 服务名称为:{}",serviceNameSpace);
      return cacheServiceMap.get(serviceNameSpace);
    } else {
      providerHost = doSelectService(instance, Constant.ROOT_PATH+serviceNameSpace);
      cacheServiceMap.put(serviceNameSpace, providerHost);
    }
    PathChildrenCache pathChildrenCache = new PathChildrenCache(instance, Constant.ROOT_PATH+serviceNameSpace, true);
    try {
      // 当Cache初始化数据后发送一个PathChildrenCacheEvent.Type#INITIALIZED事件
      pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    } catch (Exception e) {
      log.error("zk监听节点出现异常", e);
      throw new RpcException("zk监听节点出现异常");
    }
    PathChildrenCacheListener listener =
        (c, event) -> {
          log.info(
              "zk监听节点变化当前path:{} data是:{}",
              event.getData().getPath(),
              new String(event.getData().getData()));
          String serviceName = getServiceNameSpace(event.getData().getPath());
          if (cacheServiceMap.containsKey(serviceName)) {
            //更新缓存内容
            cacheServiceMap.put(serviceName, new String(event.getData().getData()));
          }
        };
    pathChildrenCache.getListenable().addListener(listener);

    return providerHost;
  }

  private String getServiceNameSpace(String path) {
    String path1 = path.replace(Constant.ROOT_PATH, "");
    String[] split = path1.split("/");
    log.info("获取节点变化后的server name 是:{}",split[0]);
    return split[0];

  }

  private String doSelectService(CuratorFramework instance, String serviceNameSpace)
      throws Exception {
    final List<String> serviceHosts = instance.getChildren().forPath(serviceNameSpace);
    return LoadBalance.selectService(serviceHosts);
  }

  private String handleServiceName(String name,String version){
    return name+"&"+version;
  }

  @Override
  public void destroy() throws Exception {
    log.info("============>>开始销毁cacheServiceMap<<===========");
    cacheServiceMap.clear();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    publishService();
  }
}
