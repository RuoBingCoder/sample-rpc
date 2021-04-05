package com.github.rpc.context.spring.annotation;

import lombok.Data;

/**
 * @author: JianLei
 * @date: 2020/9/13 12:54 下午
 * @description: 注解属性
 */
@Data
public class RocketReferenceAttribute {

    private Class<?> clazz;
    private String version;
    private String group;
    private String name;
    private String protocol;
    private Integer timeout;

    public RocketReferenceAttribute(Class<?> clazz, String version, String group, String name, String protocol, Integer timeout) {
        this.clazz = clazz;
        this.version = version;
        this.group = group;
        this.name = name;
        this.protocol = protocol;
        this.timeout = timeout;
    }

    public RocketReferenceAttribute(Class<?> clazz, String version, String group, String name) {
        this.clazz = clazz;
        this.version = version;
        this.group = group;
        this.name = name;
    }

    public RocketReferenceAttribute(Class<?> clazz, String version, String group, String name, String protocol) {
        this.clazz = clazz;
        this.version = version;
        this.group = group;
        this.name = name;
        this.protocol = protocol;
    }
}
