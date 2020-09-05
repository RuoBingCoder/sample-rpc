package com.sjl.rpc.context.zk.handle.zk;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.annotation.RpcService;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.exception.RpcException;
import com.sjl.rpc.context.mode.RpcRequest;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.zk.client.CuratorClient;
import com.sjl.rpc.context.zk.loadbalance.PollLoadBalance;
import com.sjl.rpc.context.zk.handle.abs.BaseServiceOperate;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
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
 * @description: Rpc请求处理
 */
@Component
@DependsOn("springBeanUtil")
@Slf4j
public class RpcHandler extends BaseServiceOperate implements InitializingBean, DisposableBean {

  private static final Map<String, String> cacheServiceMap = new ConcurrentHashMap<>();
  private static PollLoadBalance loadBalance;
  
  private static CuratorFramework curator=CuratorClient.instance();

  /** 发布服务 */
  @Override
  public void publishService() {
    final ConcurrentHashMap<String, Object> beans =
        SpringBeanUtil.getBeansByAnnotation(RpcService.class);
    beans.forEach(
        (beanName, bean) -> {
          try {
            final RpcService sjlRpcService = bean.getClass().getAnnotation(RpcService.class);
            final String version = sjlRpcService.version();
            boolean flag = doCheckServiceNodeIsExists(beanName, version);
            if (flag) {
              createServicePath(curator, handleServiceName(beanName, version));
            } else {
              System.out.println("***************"+getRemoteHost(handlerServiceChildPath(beanName, version)));
              cacheServiceMap.put(
                  handleServiceName(beanName, version),
                  getRemoteHost(handlerServiceChildPath(beanName, version)));
            }
          } catch (Exception e) {
            log.error("====>发布服务出现异常", e);
            throw new RpcException("发布服务出现异常");
          }
        });
  }

  /**
   * 校验zk服务是否存在节点
   *
   * @param beanName
   * @param version
   * @return
   * @throws Exception
   */
  private boolean doCheckServiceNodeIsExists(String beanName, String version) throws Exception {
    if (!"".equals(version)) {
      return !cacheServiceMap.containsKey(handleServiceName(beanName, version))
          &&curator
                  .checkExists()
                  .forPath(handleLocalNodePath(handleServiceName(beanName, version)))
              == null;
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
  private void createServicePath(CuratorFramework curator, String serviceName) throws Exception {
    String servicePath = handleLocalNodePath(serviceName);
    try {
      curator
          .create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.EPHEMERAL)
          .forPath(servicePath);
    } catch (Exception e) {
      log.error("====>create zk node error!error message is:", e);
      throw new RpcException("创建节点异常");
    }
  }

  /**
   * @param serviceName
   * @return 当前服务路径
   * @throws UnknownHostException
   */
  private String handleLocalNodePath(String serviceName) throws UnknownHostException {
    return Constant.ROOT_PATH + serviceName + "/" + getServiceLocalHost();
  }

  /**
   * 获取本地host
   * @return
   */
  private String getServiceLocalHost() throws UnknownHostException {
    return InetAddress.getLocalHost().getHostAddress()+":"+Constant.PORT;
  }

  /**
   * @param request
   * @return 负载均衡获取服务
   * @throws Exception
   */
  public String getChildNodeData(RpcRequest request) throws Exception {
    final CuratorFramework instance = curator;
    String providerHost;
    String serviceNameSpace = handleServiceName(request.getClassName(), request.getVersion());
    if (cacheServiceMap.get(serviceNameSpace) != null) {
      log.info("开始走缓存获取服务 服务名称为:{} IP:{}", serviceNameSpace,cacheServiceMap.get(serviceNameSpace));
      return cacheServiceMap.get(serviceNameSpace);
    } else {
      providerHost = doSelectService(instance, Constant.ROOT_PATH + serviceNameSpace);
      cacheServiceMap.put(serviceNameSpace, providerHost);
      log.info("cacheServiceMap value is:{}", JSONObject.toJSONString(cacheServiceMap));
    }
    PathChildrenCache pathChildrenCache =
        new PathChildrenCache(instance, Constant.ROOT_PATH + serviceNameSpace, true);
    try {
      // 当Cache初始化数据后发送一个PathChildrenCacheEvent.Type#INITIALIZED事件
      pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    } catch (Exception e) {
      log.error("zk监听节点出现异常", e);
      throw new RpcException("zk监听节点出现异常");
    }
    // 设置监听
    PathChildrenCacheListener listener =
        (c, event) -> {
          log.info(
              "zk监听节点变化当前path:{} data是:{}",
              event.getData().getPath(),
              new String(event.getData().getData()));
          String serviceName = getServiceNameSpace(event.getData().getPath());
          if (cacheServiceMap.containsKey(serviceName)) {
            // 更新缓存内容
            cacheServiceMap.put(serviceName, handleListenerChangePath(event.getData().getPath()));
          }
        };
    pathChildrenCache.getListenable().addListener(listener);

    return providerHost;
  }

  /**
   * 获取变化节点ip地址
   * @param path
   * @return
   */
  private String handleListenerChangePath(String path) {
    String[] split = path.split("/");
    return split[split.length-1];
  }

  /**
   * @param path
   * @return 获取服务命名空间
   *     <p>/rocket/api.service.IGoodsService&1.0.1/192.168.1.188 处理成api.service.IGoodsService&1.0.1
   *     和缓存数据做对比
   */
  private String getServiceNameSpace(String path) {
    String path1 = path.replace(Constant.ROOT_PATH, "");
    String[] split = path1.split("/");
    log.info("获取节点变化后的server name 是:{}", split[0]);
    return split[0];
  }

  /**
   * @param instance
   * @param serviceNameSpace
   * @return 负载均衡选取服务
   * @throws Exception
   */
  private String doSelectService(CuratorFramework instance, String serviceNameSpace)
      throws Exception {
    final List<String> serviceHosts = instance.getChildren().forPath(serviceNameSpace);
    log.info("获取child节点path是:{}", JSONObject.toJSONString(serviceHosts));
    loadBalance = SpringBeanUtil.getBean(PollLoadBalance.class);
    return loadBalance.selectService(serviceHosts);
  }
  /**
   * @Author jianlei.shi @Description 拼接服务名 @Date 4:18 下午 2020/9/3 @Param
   *
   * @return
   */
  private String handleServiceName(String name, String version) {
    return name + "&" + version;
  }
  /**
   * @Author jianlei.shi
   * @Description 拼接服务名为Dubbo path路径
   * @Date 4:18 下午 2020/9/3
   * @Param
   *
   * @return
   */
  private String handlerServiceChildPath(String name, String version) throws Exception {
    String remoteHost = getRemoteHost(Constant.ROOT_PATH + handleServiceName(name, version));
    if(remoteHost!=null){
      return Constant.ROOT_PATH + handleServiceName(name, version) + "/" + getRemoteHost(Constant.ROOT_PATH + handleServiceName(name, version));
    }
    return null;
  }

  /**
   * @return 获取远程Ip
   * @throws UnknownHostException
   */
  public static String getRemoteHost(String childPath) throws Exception {
    log.info("----->childPath is:{}",childPath);
    Stat stat = curator.checkExists().forPath(childPath);
    if (stat==null){
      return null;
    }
    List<String> hosts = curator.getChildren().forPath(childPath);

    log.info("hosts is:{}",hosts==null?"-_-":hosts.get(0));
    assert hosts != null;
    return hosts.get(0);
  }

  @Override
  public void destroy() {
    log.info("============>>开始销毁cacheServiceMap<<===========");
    cacheServiceMap.clear();
    curator.close();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    publishService();
  }

  public static void main(String[] args) throws Exception {
    Stat stat =
        curator
            .checkExists()
            .forPath("/rocket/api.service.IGoodsService&1.0.1");
    List<String> strings = CuratorClient.instance().getChildren().forPath("/rocket/api.service.IGoodsService&1.0.1/192.168.1.79:8818");
    System.out.println("-------->"+JSONObject.toJSONString(strings));
    System.out.println(stat == null);
  }
}
