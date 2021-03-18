package com.sjl.rpc.context.remote.discover.registry;

import com.sjl.rpc.context.remote.discover.service.IServiceRegistry;
import com.sjl.rpc.context.remote.discover.abs.BaseRpcHandler;
import com.sjl.rpc.context.spring.annotation.RocketService;
import com.sjl.rpc.context.exception.RocketException;
import com.sjl.rpc.context.util.SpringBeanUtil;
import com.sjl.rpc.context.remote.client.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

import static com.sjl.rpc.context.constants.Constant.PROTOCOL;

/**
 * @author: JianLei
 * @date: 2020/9/6 2:28 下午
 * @description: zk服务注册
 */
@Slf4j
@Component
@DependsOn("springBeanUtil")
public class ZkServiceRegistry extends BaseRpcHandler
        implements IServiceRegistry,  EnvironmentAware,  DisposableBean {
    private ConfigurableEnvironment env;


    @Override
    public void registry(Class<?> serviceName, String version) throws Exception {
        // 预留接口
        if (serviceName != null) {
            boolean flag = super.isExistServiceNode(serviceName.getName(), version);
            if (flag) {
                super.createServicePath(
                        CuratorClient.instance(), super.handleCacheMapServiceName(serviceName.getName(), version));
            }
            return;
        }
        final ConcurrentHashMap<String, Object> beans =
                SpringBeanUtil.getBeansByAnnotation(RocketService.class);
        beans.forEach(
                (beanName, bean) -> {
                    try {
                        final RocketService sjlRpcService = bean.getClass().getAnnotation(RocketService.class);
                        final String v = sjlRpcService.version();
                        //TODO 多协议
                        String protocol = getProtocol();
                            super.createServicePath(CuratorClient.instance(), super.handleCacheMapServiceName(beanName, v));


                    } catch (Exception e) {
                        log.error("====>发布服务出现异常", e);
                        throw new RocketException("发布服务出现异常");
                    }
                });
    }

    private String getProtocol() {
        final String proto = env.getProperty(PROTOCOL);
        if (StringUtils.isBlank(proto)) {
            throw new RocketException("protocol is null");
        }
        return proto;
    }

    @Override
    public void destroy() {
        log.info("==============开始销毁缓存map,curator关闭!================");
        cacheServiceMap.clear();
        CuratorClient.instance().close();

    }




    @Override
    protected ConfigurableEnvironment getEnvironment() {
        return this.env;
    }

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            this.env = (ConfigurableEnvironment) environment;
        }
    }
}