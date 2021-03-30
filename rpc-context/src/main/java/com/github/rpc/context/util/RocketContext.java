package com.github.rpc.context.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jianlei.shi
 * @date 2021/3/18 4:03 下午
 * @description RocketContext
 */

public class RocketContext {

    private final Map<String, String> attachments = new HashMap<>();
    /**
     * <code>
     * ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
     * table = new Entry[INITIAL_CAPACITY];
     * int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1); 计算index 使用hash计算比较慢而FastThreadLocal 则使用自增索引 用空间换时间比较快
     * table[i] = new Entry(firstKey, firstValue);
     * size = 1;
     * setThreshold(INITIAL_CAPACITY);
     * </code>
     *
     * @see io.netty.util.concurrent.FastThreadLocal
     */
    private static final ThreadLocal<RocketContext> CONTEXT_THREAD_LOCAL = ThreadLocal.withInitial(RocketContext::new);


    public static RocketContext getContext() {
        return CONTEXT_THREAD_LOCAL.get();
    }

    public Map<String, String> getAttachments() {
        remove(); //防止内存泄漏
        return attachments;
    }

    public void setAttachment(String k, String v) {
        attachments.putIfAbsent(k, v);
    }

    public static void remove() {
        CONTEXT_THREAD_LOCAL.remove();
    }
}


