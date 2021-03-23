package com.github.rpc.context.remote.loadbalance;

import cn.hutool.core.collection.CollectionUtil;
import com.github.rpc.context.remote.handler.abs.BaseLoadBalance;
import com.github.rpc.context.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: jianlei
 * @date: 2020/9/2
 * @description: 负载均衡
 */
@Component
@Slf4j
public class PollLoadBalance extends BaseLoadBalance {

    private final Lock lock = new ReentrantLock();
    private static int i = 0;

    @Override
    public <T> String selectService(T datas, String version) {
        try {
            lock.lock();
            String result = "";
            List<String> mulitServices = new ArrayList<>();
            if (datas instanceof List) {
                List services = (List) datas;
                for (Object service : services) {
                    if (service instanceof String) {
                        String data = (String) service;
                        final String version1 = StringUtils.getVersion(data);
                        if (version1.equals(version)) {
                            mulitServices.add(data);
                        }

                    }
                }

            }
            if (CollectionUtil.isNotEmpty(mulitServices)) {
                if (i >= mulitServices.size()) {
                    i = 0;
                }
                result = mulitServices.get(i++);
                log.info("load balance res:{}",result);
                return result;
            }
        } catch (Exception e) {

        } finally {
            lock.unlock();
        }
        return null;

    }
}
