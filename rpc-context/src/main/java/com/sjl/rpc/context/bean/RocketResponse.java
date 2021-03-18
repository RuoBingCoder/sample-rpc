package com.sjl.rpc.context.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: JianLei
 * @date: 2020/6/14 2:31 下午
 * @description:
 */
@Data
public class RocketResponse implements Serializable {
    private static final long serialVersionUID = 3374714188555167852L;
    /*响应ID*/
    private String responseId;
    /*异常对象*/
    private Throwable error;
    /*响应结果*/
    private Object result;

}
