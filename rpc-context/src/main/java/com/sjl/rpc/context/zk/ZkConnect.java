package com.sjl.rpc.context.zk;

import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.util.PropertiesUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;

/**
 * @author: JianLei
 * @date: 2020/8/30 4:11 下午
 * @description: zk注册
 */
public class ZkConnect {


  private static CuratorFramework curatorFramework;
  public static synchronized CuratorFramework instance() throws IOException {
    if (curatorFramework == null) {
      curatorFramework =
          CuratorFrameworkFactory.newClient(
              PropertiesUtil.getZkAddr(Constant.ZK_ADDRESS_PREFIX),
              new ExponentialBackoffRetry(1000, 3));
      curatorFramework.start();
    }
    return curatorFramework;
  }

}
