package com.github.server.goods.filter;

import com.alibaba.fastjson.JSONObject;
import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.filter.Invocation;
import com.github.rpc.context.filter.Invoker;
import com.github.rpc.context.filter.RocketFilter;
import com.github.rpc.context.spring.annotation.spi.ExtensionLoader;
import com.github.rpc.context.spring.annotation.spi.SPI;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jianlei.shi
 * @date 2021/3/19 11:21 上午
 * @description LogFilter
 */
//@SPI(name = "logFilter")
@Slf4j
public class LogFilter implements RocketFilter {
    @Override
    public RocketResponse invoke(Invoker invoker, Invocation invocation) {
        log.info("########log filter args:{} ", JSONObject.toJSONString(invocation.getParameters()));
        final RocketResponse response = invoker.invoke(invocation);
        log.info("########response is:{}",JSONObject.toJSONString(response));
        return response;
    }

}
