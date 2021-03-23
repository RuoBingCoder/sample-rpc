package com.github.rpc.context.filter;

import com.github.rpc.context.bean.RocketResponse;
import com.github.rpc.context.spring.annotation.spi.SPI;

import java.lang.reflect.Method;

/**
 * @author jianlei.shi
 * @date 2021/3/19 10:06 上午
 * @description: RocketFilter
 */
@SPI
public interface RocketFilter {

    RocketResponse invoke(Invoker invoker,Invocation invocation);

}
