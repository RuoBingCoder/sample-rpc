package com.github.rpc.context.netty.server;

import cn.hutool.core.collection.CollectionUtil;
import com.github.rpc.context.constants.Constant;
import com.github.rpc.context.netty.abs.BaseServerTransporter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: jianlei
 * @date: 2019/11/30
 * @description: NettyServer
 */
@Component
@DependsOn("springBeanUtil")
@Slf4j
public class NettyServer extends BaseServerTransporter {

    private final Map<String, Object> cacheServer = new ConcurrentHashMap<>();

    //  @PostConstruct
    public void start(Environment environment) {
        initServer(environment);
    }

    private void initServer(Environment environment) {
        if (CollectionUtil.isEmpty(cacheServer)) {
            int port = Integer.parseInt(Objects.requireNonNull(environment.getProperty(Constant.PROTOCOL_PORT)));
            try {
                String serverHost = this.getConnectHost();
                cacheServer.put(getCacheKey(serverHost, port), this);
                log.info("######server start success!");
                CompletableFuture.runAsync(() -> {
                    this.bind(port);
                    log.info("######server start success!");

                });
            } catch (UnknownHostException e) {

            }
        }
    }

    @Override
    public String getConnectHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private String getCacheKey(String serverHost, int port) {
        if (StringUtils.isNotBlank(serverHost)) {
            return serverHost + ":" + port;
        }
        return null;
    }
}
