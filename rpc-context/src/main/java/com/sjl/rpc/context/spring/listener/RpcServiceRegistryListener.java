package com.sjl.rpc.context.spring.listener;

import cn.hutool.core.collection.CollectionUtil;
import com.sjl.rpc.context.exception.RocketException;
import com.sjl.rpc.context.remote.discover.registry.ZkServiceRegistry;
import com.sjl.rpc.context.spring.annotation.EnableRocketScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * @author jianlei.shi
 * @date 2021/3/18 7:37 下午
 * @description RocketServiceRegistryListenerContext
 */
@Slf4j
public class RpcServiceRegistryListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            final ApplicationContext applicationContext = event.getApplicationContext();
            ZkServiceRegistry zkServiceRegistry = applicationContext.getBean(ZkServiceRegistry.class);
            if (zkServiceRegistry != null) {
                //registry service

                try {
                    final Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EnableRocketScan.class);
                    if (CollectionUtil.isNotEmpty(beans)) {
                        zkServiceRegistry.registry(null, null);
                    }
                } catch (Exception e) {
                    log.error("RocketServiceRegistryListener registry service error", e);
                    throw new RocketException("ocketServiceRegistryListener registry service error");
                }
            }
        }
    }
}
