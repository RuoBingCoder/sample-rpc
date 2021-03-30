package com.github.rpc.context.remote.discover.base;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.rpc.context.exception.RocketException;
import com.github.rpc.context.constants.Constant;
import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.remote.handler.abs.BaseLoadBalance;
import com.github.rpc.context.util.SpringBeanUtil;
import com.github.rpc.context.remote.client.ZookeeperClient;
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

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: JianLei
 * @date: 2020/9/1 5:28 下午
 * @description: Rpc请求处理
 */
@Slf4j
public abstract class BaseRpcHandler implements EnvironmentAware {
    protected static final Map<String, List<String>> cacheServiceMap = new ConcurrentHashMap<>();

    public static BaseLoadBalance loadBalance;

    private CuratorFramework curator;

    protected volatile Boolean isService;

    protected volatile String version;

    protected ConfigurableEnvironment env;

    protected File file;

    protected final Properties properties = new Properties();


    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            this.env = (ConfigurableEnvironment) environment;
            curator = ZookeeperClient.instance(env);

        }
    }


    /**
     * 校验zk服务是否存在节点
     *
     * @param beanName
     * @param version
     * @return
     * @throws Exception
     */
    protected boolean isExist(String beanName) throws Exception {
        if (!"".equals(version)) {
            return !(checkNode(beanName) || cacheServiceMap.containsKey(handleCacheMapServiceName(beanName)));
        }
        return true;
    }

    private boolean checkNode(String beanName) {
        try {
            final Stat stat = curator
                    .checkExists()
                    .forPath(handleCurrentServicePath(handleCacheMapServiceName(beanName)));
            return stat == null;
        } catch (Exception e) {
            log.error("check node exist exception", e);
            throw new RocketException("check node exist exception!");
        }

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
            try {
                curator
                        .create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(servicePath);
            } catch (Exception e) {
                //说明节点存在不做异常处理
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
        if (isService) {
            return Constant.ROOT_PATH + serviceName + "/" + Constant.PROVIDER + "/" + getProtocolHost() + "&" + version;
        } else {
            return Constant.ROOT_PATH + serviceName + "/" + Constant.CONSUMER + "/" + getProtocolHost() + "&" + version;

        }
    }

    protected String getMasterNodePath(String serviceName) {
        return Constant.ROOT_PATH + serviceName;
    }

    private String getProtocolHost() {
        try {
            final String port = env.getProperty(Constant.PROTOCOL_PORT);
            if (StringUtils.isBlank(port)) {
                return InetAddress.getLocalHost().getHostAddress() + ":8817";
            }
            return InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (Exception e) {
            throw new RocketException("getProtocolHost server host error!");
        }
    }

    private String serviceRegistryPath(String serviceNameSpace, String version) {
        return Constant.ROOT_PATH + serviceNameSpace + "/" + Constant.PROVIDER;
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
        String serviceNameSpace = handleCacheMapServiceName(request.getClassName());
        //hosts
        List<String> services = null;
        final String serviceNodePath = serviceRegistryPath(serviceNameSpace, request.getVersion());
        String fileName = System.getProperty("user.home") + "/rocket-rpc/provider_" + request.getVersion() + ".cache";
        if (StringUtils.isNotBlank(fileName)) {
            file = new File(fileName);
            if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Invalid registry cache file " + file + ", cause: Failed to create directory " + file.getParentFile() + "!");
                }
            }
        }
        loadProperties();
        try {
            services = curator.getChildren().forPath(serviceNodePath);
        } catch (Exception e) {
            log.error("get zookeeper node exception!", e);
            services = getCacheServicesByKey(serviceNodePath);
        }
        saveProperties(serviceNodePath, request.getVersion(), services);

        // 先进行负载
        // 1.首先判断服务名是否存在, 2.在判断IP是否存在
        loadBalance = SpringBeanUtil.getBean(BaseLoadBalance.class);
        if (cacheServiceMap.get(serviceNameSpace) != null) {
            log.info("开始走缓存获取服务 服务名称为:{} IP:{}", serviceNameSpace, cacheServiceMap.get(serviceNameSpace));
            return doSelectService(cacheServiceMap.get(serviceNameSpace), request.getVersion());
        } else {
            // 更新缓存服务地址
            cacheServiceMap.put(serviceNameSpace, services);
            providerHost = doSelectService(services, request.getVersion());
            log.info("cacheServiceMap value is:{}", JSONObject.toJSONString(cacheServiceMap));
        }
        // 设置监听
        registryListener(curator, serviceNameSpace, request.getVersion());

        return providerHost;
    }

    private List<String> getCacheServicesByKey(String serviceNodePath) {
        final String value = properties.getProperty(serviceNodePath);
        if (value == null) {
            throw new RocketException("remote service is null,retry load local cache service is null! service path is:" + serviceNodePath);
        }
        final String list = com.github.rpc.context.util.StringUtils.cleanLastSymbol(value);
        if (list.contains(",")) {
            String[] split = list.split(",");
            return Arrays.asList(split);
        }
        return null;
    }

    private void saveProperties(String serviceRegistryPath, String version, List<String> services) throws IOException {
        file = new File(System.getProperty("user.home") + "/rocket-rpc/provider_" + version + ".cache");
        if (!file.exists()) {
            file.createNewFile();
        }
        properties.setProperty(serviceRegistryPath, toString(services));
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            properties.store(fileOutputStream, "rocket-rpc Cache");
        }
    }

    private String toString(List<String> services) {
        StringBuilder sb = new StringBuilder();
        for (String service : services) {
            sb.append(service).append(",");
        }
        return sb.toString();
    }

    private void loadProperties() {
        if (file != null && file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                properties.load(in);
                if (log.isInfoEnabled()) {
                    log.info("Load registry cache file " + file + ", data: " + properties);
                }
            } catch (Throwable e) {
                log.warn("Failed to load registry cache file " + file, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
    }


    /**
     * 注册监听节点事件
     *
     * @param curator
     * @param nameSpace
     * @param serviceNameSpace
     */
    private void registryListener(CuratorFramework curator, String serviceNameSpace, String version) {
        PathChildrenCache pathChildrenCache =
                new PathChildrenCache(curator, serviceRegistryPath(serviceNameSpace, version), true);
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
                            curator.getChildren().forPath(serviceRegistryPath(serviceNameSpace, version));
                    //update local cache
                    saveProperties(serviceRegistryPath(serviceNameSpace, version), version, services);
                    cacheServiceMap.put(serviceNameSpace, services);
                };
        pathChildrenCache.getListenable().addListener(listener);
    }

    /**
     * @param serviceNameSpace
     * @param version
     * @return 负载均衡选取服务
     * @throws Exception
     */
    private String doSelectService(List<String> serviceNameSpace, String version) throws Exception {
        return loadBalance.loadBalance(serviceNameSpace, version);
    }

    /**
     * @return
     * @Author jianlei.shi @Description 拼接服务名 @Date 4:18 下午 2020/9/3 @Param
     */
    protected String handleCacheMapServiceName(String name) {
        return name;
    }

    protected abstract void registry(Class<?> serviceName, Object service, String version, Boolean isService);


    public static void main(String[] args) throws Exception {
//        Stat stat = curator.checkExists().forPath("/rocket/api.service.IGoodsService&1.0.1");
//        CuratorClient.instance().create().withMode(CreateMode.PERSISTENT).forPath("/test");
//        CuratorClient.instance().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/test/01");
//        List<String> strings =
//                CuratorClient.instance().getChildren().forPath("/test/api.service.IGoodsService&1.0.1");
//        System.out.println("-------->" + JSONObject.toJSONString(strings));
        //    /rocket/api.service.IGoodsService/provider/192.168.1.14:8817&1.0.1
        /*Stat stat = curator
                .checkExists()
                .forPath("/rocket/api.service.IGoodsService/provider/192.168.1.14:8817&1.0.1");
        System.out.println(stat == null);*/

    }
}
