package com.github.rpc.context.netty.helper;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author jianlei.shi
 * @date 2021/3/16 2:05 下午
 * @description TaskThreadPoolHelper
 */
@Slf4j
public class TaskThreadPoolHelper {

    private static final List<ExecutorService> executorServices = new ArrayList<>();


    public static void signalStartClear() {
        if (CollectionUtil.isNotEmpty(executorServices)) {
            for (ExecutorService executor : executorServices) {
                log.info("netty connect <destroy> thread info:{}", Thread.currentThread().getName());
                executor.shutdown();
            }
        } else {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static ExecutorService getExecutor() {
        return Executors.newFixedThreadPool(1);

    }


    public static void addExecutor(ExecutorService executor) {
        if (!executorServices.contains(executor)) {
            executorServices.add(executor);
        }
    }


}
