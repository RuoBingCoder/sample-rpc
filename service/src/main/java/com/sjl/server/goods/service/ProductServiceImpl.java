package com.sjl.server.goods.service;

import api.service.IProductService;
import com.sjl.rpc.context.annotation.RocketService;

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
