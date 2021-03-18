package com.sjl.rpc.context.remote.discover.service;

import com.sjl.rpc.context.bean.RocketRequest;

/**
 * @author: JianLei
 * @date: 2020/9/6 2:38 下午
 * @description:
 */

public interface IRocketServiceDiscover {


    String selectService(RocketRequest request) throws Exception;
}
