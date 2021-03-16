package com.sjl.rpc.context.remote.handler.abs;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.exception.RocketException;
import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.remote.client.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: JianLei
 * @date: 2020/9/1 5:28 下午
 * @description: Rpc请求处理
 */
@Slf4j
public abstract class BaseRpcHandler {
  protected static final Map<String, List<String>> cacheServiceMap = new ConcurrentHashMap<>();
  public static BaseLoadBalance loadBalance;
  private static final CuratorFramework curator = CuratorClient.instance();

  /**
   * 校验zk服务是否存在节点
   *
   * @param beanName
   * @param version
   * @return
   * @throws Exception
   */
  protected boolean doCheckServiceNodeIsExists(String beanName, String version) throws Exception {
    if (!"".equals(version)) {
      return cacheServiceMap.containsKey(handleCacheMapServiceName(beanName, version))
          && curator
                  .checkExists()
                  .forPath(handleCurrentServicePath(handleCacheMapServiceName(beanName, version)))
              != null;
    }
    return false;
  }

  /**
   * 创建zk节点,即dubbo服务
   *
   * @param curator
   * @param serviceName
   * @throws Exception
   */
  protected void createServicePath(CuratorFramework curator, String serviceName) throws Exception {
    String servicePath = handleCurrentServicePath(serviceName);
    try {
      curator
          .create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.EPHEMERAL)
          .forPath(servicePath);
    } catch (Exception e) {
      log.error("====>create zk node error!error message is:", e);
      throw new RocketException("创建节点异常");
    }
  }

  /**
   * @param serviceName
   * @return 当前服务路径
   * @throws UnknownHostException
   */
  protected String handleCurrentServicePath(String serviceName) throws UnknownHostException {
    return Constant.ROOT_PATH + serviceName + "/" + getServiceHostAndPort();
  }

  private String serviceRegistryPath(String serviceName) {
    return Constant.ROOT_PATH + serviceName;
  }

  /**
   * 获取本地host
   *
   * @return
   */
  private String getServiceHostAndPort() throws UnknownHostException {
    return InetAddress.getLocalHost().getHostAddress() + ":" + Constant.PORT;
  }

  /**
   * @param request
   * @return 负载均衡获取服务
   * @throws Exception
   */
  protected String getChildNodePath(RocketRequest request) throws Exception {
    String providerHost;
    String serviceNameSpace =
        handleCacheMapServiceName(request.getClassName(), request.getVersion());
    //hosts
    List<String> services = curator.getChildren().forPath(serviceRegistryPath(serviceNameSpace));
    if (CollectionUtil.isEmpty(services)) {
      throw new RocketException("远程服务列表为空");
    }
    // 先进行负载
    // 1.首先判断服务名是否存在, 2.在判断IP是否存在
    loadBalance = SpringBeanUtil.getBean(BaseLoadBalance.class);
    if (cacheServiceMap.get(serviceNameSpace) != null) {
      log.info("开始走缓存获取服务 服务名称为:{} IP:{}", serviceNameSpace, cacheServiceMap.get(serviceNameSpace));
      return doSelectService(cacheServiceMap.get(serviceNameSpace));
    } else {
      // 更新缓存服务地址
      cacheServiceMap.put(serviceNameSpace, services);
      providerHost = doSelectService(services);
      log.info("cacheServiceMap value is:{}", JSONObject.toJSONString(cacheServiceMap));
    }
    // 设置监听
    registryListener(curator, serviceNameSpace);

    return providerHost;
  }

  /**
   * 注册监听节点事件
   *
   * @param curator
   * @param serviceNameSpace
   */
  private void registryListener(CuratorFramework curator, String serviceNameSpace) {
    PathChildrenCache pathChildrenCache =
        new PathChildrenCache(curator, serviceRegistryPath(serviceNameSpace), true);
    try {
      // 当Cache初始化数据后发送一个PathChildrenCacheEvent.Type#INITIALIZED事件
      pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    } catch (Exception e) {
      log.error("zk监听节点出现异常", e);
      throw new RocketException("zk监听节点出现异常");
    }
    PathChildrenCacheListener listener =
        (c, event) -> {
          log.info(
              "zk监听节点变化当前path:{} data是:{}",
              event==null||event.getData()==null?"--":event.getData().getPath(),
              event==null||event.getData()==null?"==":new String(event.getData().getData()));
          // 更新缓存
          List<String> services =
              curator.getChildren().forPath(serviceRegistryPath(serviceNameSpace));
          cacheServiceMap.put(serviceNameSpace, services);
        };
    pathChildrenCache.getListenable().addListener(listener);
  }

  /**
   * @param serviceNameSpace
   * @return 负载均衡选取服务
   * @throws Exception
   */
  private String doSelectService(List<String> serviceNameSpace) throws Exception {
    return loadBalance.loadBalance(serviceNameSpace);
  }
  /**
   * @Author jianlei.shi @Description 拼接服务名 @Date 4:18 下午 2020/9/3 @Param
   *
   * @return
   */
  protected String handleCacheMapServiceName(String name, String version) {
    return name + "&" + version;
  }

  public static void main(String[] args) throws Exception {
    Stat stat = curator.checkExists().forPath("/rocket/api.service.IGoodsService&1.0.1");
    List<String> strings =
        CuratorClient.instance().getChildren().forPath("/rocket/api.service.IGoodsService&1.0.1");
    System.out.println("-------->" + JSONObject.toJSONString(strings));
    System.out.println(stat == null);
  }
}
