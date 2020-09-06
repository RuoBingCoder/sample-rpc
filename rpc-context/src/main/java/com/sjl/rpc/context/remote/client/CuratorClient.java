package com.sjl.rpc.context.remote.client;

import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author: JianLei
 * @date: 2020/8/30 4:11 下午
 * @description: zk注册
 */
@Slf4j
public class CuratorClient {

  private static CuratorFramework curatorFramework;

  public static synchronized CuratorFramework instance() {
    if (curatorFramework == null) {
      log.info("------------------------开始创建ZK连接---------------------------------");
      try {
        curatorFramework =
            CuratorFrameworkFactory.newClient(
                PropertiesUtil.getZkAddr(Constant.ZK_ADDRESS_PREFIX),
                new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
      } catch (Exception e) {
        log.error("ZK启动异常", e);
      }
      log.info("------------------------curator 启动成功！---------------------------------");
    }
    return curatorFramework;
  }
}
