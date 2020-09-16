package com.sjl.rpc.context.annotation.spi;

/**
 * @author: JianLei
 * @date: 2020/9/16 3:13 下午
 * @description:
 */

public class Holder<T> {

    private volatile T value;

    public Holder() {
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }
}
