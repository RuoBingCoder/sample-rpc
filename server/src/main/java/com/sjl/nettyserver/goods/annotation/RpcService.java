package com.sjl.nettyserver.goods.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author: JianLei
 * @date: 2020/6/14 4:06 下午
 * @description:
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {

  Class<?> value();
}
