package com.github.rpc.context.remote.client;

import com.github.rpc.context.constants.Constant;
import com.github.rpc.context.exception.RocketException;
import com.github.rpc.context.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author: JianLei
 * @date: 2020/8/30 4:11 下午
 * @description: zk注册
 */
@Slf4j
public class ZookeeperClient {

    private static CuratorFramework curatorFramework;

    public static synchronized CuratorFramework instance(ConfigurableEnvironment env) {
        if (curatorFramework == null) {
            log.info("------------------------开始创建ZK连接---------------------------------");
            checkEnv(env);
            try {
                curatorFramework =
                        CuratorFrameworkFactory.newClient(
                                PropertiesUtil.getZkAddr(env, Constant.ZK_ADDRESS_PREFIX),
                                new ExponentialBackoffRetry(1000, 3));
                curatorFramework.start();
            } catch (Exception e) {
                log.error("ZK启动异常", e);
            }
            log.info("------------------------curator 启动成功！---------------------------------");
        }
        return curatorFramework;
    }

    private static void checkEnv(ConfigurableEnvironment env) {
        if (env == null) {
            throw new RocketException("CuratorClient env is null");
        }
    }
}
