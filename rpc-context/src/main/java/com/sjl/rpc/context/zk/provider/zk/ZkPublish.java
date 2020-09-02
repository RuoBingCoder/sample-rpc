package com.sjl.rpc.context.zk.provider.zk;

import com.sjl.rpc.context.annotation.SjlRpcService;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.zk.ZkConnect;
import com.sjl.rpc.context.zk.loadbalance.LoadBlance;
import com.sjl.rpc.context.zk.provider.BaseServiceOperate;
import lombok.Value;
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
public class ZkPublish extends BaseServiceOperate implements InitializingBean, DisposableBean {

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
              if (!cacheServiceMap.containsKey(beanName + "&" + version)
                  || ZkConnect.instance().checkExists().forPath(beanName + "&" + version) == null) {
                createServicePath(ZkConnect.instance(), beanName + "&" + version);
              } else {
                cacheServiceMap.put(
                    beanName + "&" + version, InetAddress.getLocalHost().getHostAddress());
              }
            }
          } catch (Exception e) {
            log.error("发布服务出现异常", e);
          }
        });
  }

  private void createServicePath(CuratorFramework curator, String serviceName) throws Exception {
    String servicePath =
        Constant.ROOT_PATH + serviceName + "/" + InetAddress.getLocalHost().getHostAddress();
    curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath);
  }

  public String getData(RpcRequest request) throws Exception {
    final CuratorFramework instance = ZkConnect.instance();
    String providerHost;
    String serviceNameSpace =
        Constant.ROOT_PATH + request.getClassName() + "&" + request.getVersion();
    if (cacheServiceMap.get(serviceNameSpace) != null) {
      log.info("开始走缓存获取服务");
      return cacheServiceMap.get(serviceNameSpace);
    } else {
      providerHost = doSelectService(instance, serviceNameSpace);
      cacheServiceMap.put(serviceNameSpace, providerHost);
    }
    PathChildrenCache pathChildrenCache = new PathChildrenCache(instance, serviceNameSpace, true);
    PathChildrenCacheListener listener =
        (c, event) -> {
          if (cacheServiceMap.containsKey(event.getData().getPath())) {
            log.info(
                "zk监听节点变化当前path:{} data是:{}",
                event.getData().getPath(),
                new String(event.getData().getData()));
            cacheServiceMap.remove(event.getData().getPath());
            cacheServiceMap.put(event.getData().getPath(), new String(event.getData().getData()));
          }
        };
    pathChildrenCache.getListenable().addListener(listener);
    return providerHost;
  }

  private String doSelectService(CuratorFramework instance, String serviceNameSpace)
      throws Exception {
    final List<String> serviceHosts = instance.getChildren().forPath(serviceNameSpace);
    return LoadBlance.selectService(serviceHosts);
  }

  @Override
  public void destroy() throws Exception {
    cacheServiceMap.clear();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    publishService();
  }
}
