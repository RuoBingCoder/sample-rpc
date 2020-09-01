package com.sjl.rpc.context.zk.provider.zk;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.zk.ZkConnect;
import com.sjl.rpc.context.zk.provider.ServiceProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

/**
 * @author: JianLei
 * @date: 2020/9/1 5:28 下午
 * @description:
 */
@Component
public class ZkPublish implements ServiceProvider {

  private Set<String> cacheServiceSet = new ConcurrentHashSet<>();

  @Override
  public void publishService(String serviceName) throws Exception {
    if (!cacheServiceSet.contains(serviceName)
        || ZkConnect.instance().checkExists().forPath(serviceName) == null) {
      createServicePath(ZkConnect.instance(), serviceName);
    } else {
      cacheServiceSet.add(serviceName);
    }
  }

  private void createServicePath(CuratorFramework curator, String serviceName) throws Exception {
    curator
        .create()
        .creatingParentsIfNeeded()
        .withMode(CreateMode.PERSISTENT)
        .forPath(
            Constant.ROOT_PATH + serviceName + "/" + InetAddress.getLocalHost().getHostAddress());
  }
}
