package com.github.rpc;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;

/**
 * @author jianlei.shi
 * @date 2021/3/27 1:34 下午
 * @description FastThreadLocalDemo
 */

public class FastThreadLocalDemo {
    private final FastThreadLockTest fastThreadLockTest;

    public FastThreadLocalDemo() {
        this.fastThreadLockTest = new FastThreadLockTest();
    }

    public static FastThreadLocalDemo instance() {
        return new FastThreadLocalDemo();
    }


    public static void main(String[] args) {
        final FastThreadLocalDemo threadLocalDemo = FastThreadLocalDemo.instance();
        new Thread(() -> {
//             Object obj = threadLocalDemo.fastThreadLockTest.get();
            try {
                for (int i = 0; i < 10; i++) {
                    threadLocalDemo.fastThreadLockTest.set(i+"");
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }).start();


        new Thread(() -> {
//            Object obj = threadLocalDemo.fastThreadLockTest.get();
            try {
                for (int i = 0; i < 10; i++) {
                    Object shareObj = threadLocalDemo.fastThreadLockTest.get();
                    System.out.println("===>"+shareObj);
                    Thread.sleep(1000);

                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }
}
