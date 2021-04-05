package com.github.rpc.context.remote.time;

import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.bean.RocketResponse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jianlei.shi
 * @date 2021/4/5 2:09 下午
 * @description Timer
 */
@Setter
@Getter
@Slf4j
public class Timer {


    private RocketRequest request;

    private Integer timeout;

    private LinkedBlockingQueue<RocketResponse> responses;

    private Thread thread;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("simple-rpc-task-%s").build();

    public Timer(RocketRequest request, Integer timeout, LinkedBlockingQueue<RocketResponse> responses) {
        this.request = request;
        this.timeout = timeout;
        this.responses = responses;
        this.thread = threadFactory.newThread(new Task(request, timeout, responses));
    }

    public void
    start() {
        try {
            thread.start();
        } catch (Exception e) {
            log.error("Timer thread start error :", e);
        }
    }

    @Data
    static class Task implements Runnable {

        private RocketRequest request;

        private Integer timeout;

        private LinkedBlockingQueue<RocketResponse> responses;

        private AtomicBoolean isDone = new AtomicBoolean(false);

        public Task(RocketRequest request, Integer timeout, LinkedBlockingQueue<RocketResponse> responses) {
            this.request = request;
            this.timeout = timeout;
            this.responses = responses;
        }

        @Override
        public void run() {
            final long startTime = System.currentTimeMillis();
            while (!isDone.get()) {
                if (isTimeout(startTime)) {
                    System.out.println("timeout!!!");
                    RocketResponse response = new RocketResponse();
                    response.setResponseId(request.getRequestId());
                    response.setResult("this task invoke timeout! please check interface! interface is: 【" + request.getClassName() + "#" + request.getMethodName() + "】");
                    responses.add(response);
                    isDone.set(true);
                }

            }
        }


        private boolean isTimeout(long startTime) {
            return (System.currentTimeMillis() - (startTime + timeout)) > 0;

        }
    }


}
