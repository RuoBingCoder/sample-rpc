package com.github.rpc.context.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: JianLei
 * @date: 2020/6/14 2:35 下午
 * @description:
 */
@Data
public class RocketRequest implements Serializable {

    private static final long serialVersionUID = -2625585669090924236L;


    /*请求ID*/
    private String requestId;
    /*调用class类名*/
    private String className;
    /*调用方法名*/
    private String methodName;
    /*调用参数类型集合*/
    private Class<?>[] parameterTypes;
    /*调用参数集合*/
    private Object[] parameters;
    /**
     * 版本号
     */
    private String version;
    /**
     * 心跳包msg
     */
    private String heartPackMsg;

    /**
     * 而外的参数
     */

    private final Map<String, String> rpcAttachments = new ConcurrentHashMap<>();


    /**
     * 超时时间
     */
    private Integer timeout;

    public void setAttachment(String key, String value) {
        rpcAttachments.putIfAbsent(key, value);

    }

    public Object getAttachment(String key) {
        return rpcAttachments.get(key);

    }
    public void setAttachments( Map<String, String> rat) {
        rpcAttachments.putAll(rat);

    }


}
