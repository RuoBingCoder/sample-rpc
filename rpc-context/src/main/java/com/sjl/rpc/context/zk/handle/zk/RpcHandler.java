package com.sjl.rpc.context.zk.handle.zk;

import com.sjl.rpc.context.annotation.RpcService;
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
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

  /**
   * 发布服务
   */
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
              createServicePath(CuratorClient.instance(), handleServiceName(beanName, version));
            } else {
              cacheServiceMap.put(
                  handleServiceName(beanName, version),
                  getRemoteHost());
            }
          } catch (Exception e) {
            log.error("====>发布服务出现异常", e);
            throw new RpcException("发布服务出现异常");
          }
        });
  }

  /**
   * 校验zk服务是否存在节点
   * @param beanName
   * @param version
   * @return
   * @throws Exception
   */
  private boolean doCheckServiceNodeIsExists(String beanName, String version) throws Exception {
    if (!"".equals(version)) {
      return !cacheServiceMap.containsKey(handleServiceName(beanName, version))
          && CuratorClient.instance()
                  .checkExists()
                  .forPath(handlerServiceChildPath(beanName, version))
              == null;
    }
    return false;
  }

  /**
   * 创建zk节点,即dubbo服务
   * @param curator
   * @param serviceName
   * @throws Exception
   */
  private void createServicePath(CuratorFramework curator, String serviceName) throws Exception {
    String servicePath = handleZkNodePath(serviceName);
    try {
      curator
          .create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.PERSISTENT)
          .forPath(servicePath);
    } catch (Exception e) {
      log.error("====>create zk node error!error message is:", e);
      throw new RpcException("创建节点异常");
    }
  }

  /**
   *
   * @param serviceName
   * @return 当前服务路径
   * @throws UnknownHostException
   */
  private String handleZkNodePath(String serviceName) throws UnknownHostException {
    return Constant.ROOT_PATH + serviceName + "/" + getRemoteHost();
  }

  /**
   *
   * @param request
   * @return 负载均衡获取服务
   * @throws Exception
   */
  public String getChildNodeData(RpcRequest request) throws Exception {
    final CuratorFramework instance = CuratorClient.instance();
    String providerHost;
    String serviceNameSpace = handleServiceName(request.getClassName(), request.getVersion());
    if (cacheServiceMap.get(serviceNameSpace) != null) {
      log.info("开始走缓存获取服务 服务名称为:{}", serviceNameSpace);
      return cacheServiceMap.get(serviceNameSpace);
    } else {
      providerHost = doSelectService(instance, Constant.ROOT_PATH + serviceNameSpace);
      cacheServiceMap.put(serviceNameSpace, providerHost);
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
    PathChildrenCacheListener listener =
        (c, event) -> {
          log.info(
              "zk监听节点变化当前path:{} data是:{}",
              event.getData().getPath(),
              new String(event.getData().getData()));
          String serviceName = getServiceNameSpace(event.getData().getPath());
          if (cacheServiceMap.containsKey(serviceName)) {
            // 更新缓存内容
            cacheServiceMap.put(serviceName, new String(event.getData().getData()));
          }
        };
    pathChildrenCache.getListenable().addListener(listener);

    return providerHost;
  }

  /**
   *
   * @param path
   * @return 获取服务命名空间
   * <P>/rocket/api.service.IGoodsService&1.0.1/192.168.1.188 处理成api.service.IGoodsService&1.0.1
   * 和缓存数据做对比
   * </P>
   */
  private String getServiceNameSpace(String path) {
    String path1 = path.replace(Constant.ROOT_PATH, "");
    String[] split = path1.split("/");
    log.info("获取节点变化后的server name 是:{}", split[0]);
    return split[0];
  }

  /**
   *
   * @param instance
   * @param serviceNameSpace
   * @return 负载均衡选取服务
   * @throws Exception
   */
  private String doSelectService(CuratorFramework instance, String serviceNameSpace)
      throws Exception {
    final List<String> serviceHosts = instance.getChildren().forPath(serviceNameSpace);
    return LoadBalance.selectService(serviceHosts);
  }
  /**
   * @Author jianlei.shi
   * @Description 拼接服务名
   * @Date 4:18 下午 2020/9/3
   * @Param
   * @return
   **/
  private String handleServiceName(String name, String version) {
    return name + "&" + version;
  }
  /**
   * @Author jianlei.shi
   * @Description 拼接服务名为Dubbo path路径
   * @Date 4:18 下午 2020/9/3
   * @Param
   * @return
   **/
  private String handlerServiceChildPath(String name, String version) throws UnknownHostException {
    return Constant.ROOT_PATH
        + handleServiceName(name, version)
        + "/"
        + getRemoteHost();
  }

  /**
   *
   * @return 获取本地Ip
   * @throws UnknownHostException
   */
  public static String getRemoteHost() throws UnknownHostException {
    return InetAddress.getLocalHost().getHostAddress();
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

  public static void main(String[] args) throws Exception {
    Stat stat =
        CuratorClient.instance()
            .checkExists()
            .forPath("/rocket/api.service.IGoodsService&1.0.1/192.168.1.188");
    System.out.println(stat == null);
  }
}
