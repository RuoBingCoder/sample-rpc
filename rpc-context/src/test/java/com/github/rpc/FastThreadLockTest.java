package com.github.rpc;

import io.netty.util.concurrent.FastThreadLocal;

/**
 * @author jianlei.shi
 * @date 2021/3/27 1:40 下午
 * @description FastThreadLockTest
 */

public class FastThreadLockTest extends FastThreadLocal<Object> {

    @Override
    protected Object initialValue() throws Exception {
        return new Object();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
