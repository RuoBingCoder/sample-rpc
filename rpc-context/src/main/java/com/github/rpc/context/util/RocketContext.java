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

    private static final ThreadLocal<RocketContext> CONTEXT_THREAD_LOCAL = ThreadLocal.withInitial(RocketContext::new);


    public static RocketContext getContext() {
        return CONTEXT_THREAD_LOCAL.get();
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachment(String k, String v) {
        attachments.putIfAbsent(k, v);
    }

    public void remove() {
        CONTEXT_THREAD_LOCAL.remove();
    }
}


