package com.github.server.goods.spi;

/**
 * @author: JianLei
 * @date: 2020/9/16 2:30 下午
 * @description:
 */

public class SpiServiceImpl2 implements SpiService {
    @Override
    public String testSpiService(String parameter) {
        return "SpiServiceImpl2->"+parameter;
    }
}
