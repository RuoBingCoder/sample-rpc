package com.sjl.rpc.context.remote.discover;

import com.sjl.rpc.context.mode.RpcRequest;

/**
 * @author: JianLei
 * @date: 2020/9/6 2:38 下午
 * @description:
 */

public interface IRpcServiceDiscover {


    String selectService(RpcRequest request) throws Exception;
}
