package com.sjl.rpc.context.exception;

/**
 * @author: JianLei
 * @date: 2020/9/3 1:55 下午
 * @description:
 */

public class RpcException extends RuntimeException {
    public RpcException(String message) {
        super(message);
    }
}
