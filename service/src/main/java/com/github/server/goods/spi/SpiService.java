package com.github.server.goods.spi;

import com.github.rpc.context.spring.annotation.spi.SPI;

/**
 * @author: JianLei
 * @date: 2020/9/16 2:28 下午
 * @description:
 */
@SPI(name = "test1")
public interface SpiService {

    String testSpiService(String parameter);
}
