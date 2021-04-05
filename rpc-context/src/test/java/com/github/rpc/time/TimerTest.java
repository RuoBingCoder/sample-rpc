package com.github.rpc.time;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jianlei.shi
 * @date 2021/4/5 3:13 下午
 * @description TimerTest
 */

public class TimerTest {

    static class Task implements Runnable {

        private AtomicBoolean isDone = new AtomicBoolean(false);

        @Override
        public void run() {
            final long startTime = System.currentTimeMillis();

            System.out.println("hdhzz");
            while (!isDone.get()) {
                if (isTimeout(startTime)) {
                    System.out.print("abc");
                    isDone.set(true);
                }
            }
        }

        private boolean isTimeout(long startTime) {
            return (System.currentTimeMillis() - (startTime + 5000)) > 0;


        }
    }

}
