package com.sjl.nettyserver.goods.zk;

import com.sjl.nettyserver.goods.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.CountDownLatch;

/**
 * @author: jianlei
 * @date: 2020/6/17
 * @description: ZkServer
 */
@Slf4j
public class ZkRegister {

//  public static final String ZK_ADDR = "39.108.137.115:2181";

  public static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);
  private  String registerAddr;

  public ZkRegister(String registerAddr) {
    this.registerAddr = registerAddr;
  }

  public ZkRegister() {

  }

  public synchronized void register() throws Exception {
    String zkAddr = PropertiesUtil.getProperties("zk.addr");
    log.info("-----【获取zk地址是:{}】-----", zkAddr);
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zkAddr, retryPolicy);
    curatorFramework.start();
    COUNT_DOWN_LATCH.countDown();
    try {
      log.info("zk开始注册...");
      curatorFramework
          .create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
          .forPath("/register/addr", registerAddr.getBytes());

    } catch (Exception e) {
      log.error("zk注册出现异常", e);
    }
  }
}
