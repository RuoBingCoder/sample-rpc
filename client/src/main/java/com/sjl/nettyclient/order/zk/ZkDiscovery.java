package com.sjl.nettyclient.order.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.CountDownLatch;

/**
 * @author: jianlei
 * @date: 2020/6/19
 * @description: zk服务发现
 */
@Slf4j
public class ZkDiscovery {

  public static final String ZK_ADDR = "39.108.137.115:2181";

  public synchronized String getService() throws Exception {
    String paths = null;
    try {

      RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
      final CuratorFramework framework =
          CuratorFrameworkFactory.builder()
              .connectString(ZK_ADDR)
              .sessionTimeoutMs(5000)
              .retryPolicy(retryPolicy)
              .build();

      framework.start();
      String childPath = framework.getChildren().forPath("/register").get(0);
      paths = new String(framework.getData().forPath("/register/"+childPath));
      log.info("-----path:{}", paths);
    } catch (Exception e) {
      log.error("获取服务出现异常", e);
    }
    return paths;
  }
}
