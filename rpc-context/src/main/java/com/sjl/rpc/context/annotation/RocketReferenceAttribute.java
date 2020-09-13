package com.sjl.rpc.context.annotation;

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

    public RocketReferenceAttribute(Class<?> clazz, String version, String group, String name) {
        this.clazz = clazz;
        this.version = version;
        this.group = group;
        this.name = name;
    }
}
