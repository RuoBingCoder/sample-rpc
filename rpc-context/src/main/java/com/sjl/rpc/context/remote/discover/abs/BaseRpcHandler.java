package com.sjl.rpc.context.remote.discover.abs;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.exception.RocketException;
import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.remote.handler.abs.BaseLoadBalance;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.remote.client.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

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
    protected ConfigurableEnvironment environment;

    protected abstract ConfigurableEnvironment getEnvironment();

    /**
     * 校验zk服务是否存在节点
     *
     * @param beanName
     * @param version
     * @return
     * @throws Exception
     */
    protected boolean isExistServiceNode(String beanName, String version) throws Exception {
        if (!"".equals(version)) {
            return isNodeExist(beanName, version) && !cacheServiceMap.containsKey(handleCacheMapServiceName(beanName, version));
        }
        return false;
    }

    private boolean isNodeExist(String beanName, String version) {
        try {
            final Stat stat = curator
                    .checkExists()
                    .forPath(handleCurrentServicePath(handleCacheMapServiceName(beanName, version)));
            if (stat != null) {
                return true;
            }
        } catch (Exception e) {
            log.error("check node exist exception", e);
            throw new RocketException("check node exist exception!");
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
            final String[] split = serviceName.split("&");
            final String version = split[1];
            //双重校验
            boolean flag = isExistServiceNode(serviceName, version);
            if (!flag) {
                try {
                    curator
                            .create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.EPHEMERAL)
                            .forPath(servicePath);
                } catch (Exception e) {
                    //说明节点存在不做异常处理
                }

            }
        } catch (Exception e) {
            log.error("====>create zk node error!error message is:", e);
            if (e instanceof KeeperException) {
                KeeperException kex = (KeeperException) e;
                if (KeeperException.Code.NODEEXISTS.name().equals(kex.code().name())) {
                    log.error("===========node exist !");
                } else {
                    throw new RocketException("创建节点异常");

                }
            }
        }
    }

    /**
     * @param serviceName
     * @return 当前服务路径
     * @throws UnknownHostException
     */
    protected String handleCurrentServicePath(String serviceName) throws UnknownHostException {
        return Constant.ROOT_PATH + serviceName + "/" + getProtocolHost();
    }

    protected String getMasterNodePath(String serviceName) {
        return Constant.ROOT_PATH + serviceName;
    }

    private String getProtocolHost() {
         try {
             final String port = getEnvironment().getProperty(Constant.PROTOCOL_PORT);
             if (StringUtils.isBlank(port)) {
                 return InetAddress.getLocalHost().getHostAddress() + ":8817";
             }
             return InetAddress.getLocalHost() + ":" + port;
         } catch (Exception e) {
            throw new RocketException("getProtocolHost server host error!");
         }
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
                            event == null || event.getData() == null ? "--" : event.getData().getPath(),
                            event == null || event.getData() == null ? "==" : new String(event.getData().getData()));
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
     * @return
     * @Author jianlei.shi @Description 拼接服务名 @Date 4:18 下午 2020/9/3 @Param
     */
    protected String handleCacheMapServiceName(String name, String version) {
        return name + "&" + version;
    }

    public static void main(String[] args) throws Exception {
//        Stat stat = curator.checkExists().forPath("/rocket/api.service.IGoodsService&1.0.1");
        CuratorClient.instance().create().withMode(CreateMode.PERSISTENT).forPath("/test");
        CuratorClient.instance().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/test/01");
//        List<String> strings =
//                CuratorClient.instance().getChildren().forPath("/test/api.service.IGoodsService&1.0.1");
//        System.out.println("-------->" + JSONObject.toJSONString(strings));
    }
}
