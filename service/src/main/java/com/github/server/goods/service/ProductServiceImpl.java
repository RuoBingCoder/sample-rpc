package com.github.server.goods.service;

import api.service.IProductService;
import com.github.rpc.context.spring.annotation.RocketService;

/**
 * @author: JianLei
 * @date: 2020/9/1 3:28 下午
 * @description:
 */
@RocketService(value = IProductService.class,version = "1.0.1")
public class ProductServiceImpl implements IProductService {
    @Override
    public int count(Long productId) {
        return 12;
    }
}
