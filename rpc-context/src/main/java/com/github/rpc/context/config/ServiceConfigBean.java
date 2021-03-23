package com.github.rpc.context.config;

import cn.hutool.core.collection.CollectionUtil;
import com.github.rpc.context.constants.Constant;
import com.github.rpc.context.exception.RocketException;
import com.github.rpc.context.netty.server.NettyServer;
import com.github.rpc.context.remote.discover.registry.ZkServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jianlei.shi
 * @date 2021/3/23 3:36 下午
 * @description ServiceConfig
 */
@Slf4j
public class ServiceConfigBean<T> implements InitializingBean, EnvironmentAware, ApplicationContextAware {

    private Environment environment;
    private final T service;
    private ApplicationContext applicationContext;

    public ServiceConfigBean(T service) {
        this.service = service;
    }


    @Override
    public void afterPropertiesSet() {
        try {
            doExport();
        } catch (Exception e) {
            log.error("export service exception :", e);
            throw new RocketException("服务导出异常");
        }
    }

    private void doExport() {
        log.info("########ServiceConfigBean doExport beanName:{}", service.getClass().getName());
        //先注册服务
        final ZkServiceRegistry zkServiceRegistry = applicationContext.getBean(ZkServiceRegistry.class);
        zkServiceRegistry.registry(null, service, null, true);
        //启动服务确保1次
        final NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        nettyServer.start(environment);


    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
