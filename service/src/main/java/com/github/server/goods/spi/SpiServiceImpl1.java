package com.github.server.goods.spi;

/**
 * @author: JianLei
 * @date: 2020/9/16 2:29 下午
 * @description:
 */

public class SpiServiceImpl1 implements SpiService {
    @Override
    public String testSpiService(String parameter) {
        return "SpiServiceImpl1->"+parameter;
    }
}
