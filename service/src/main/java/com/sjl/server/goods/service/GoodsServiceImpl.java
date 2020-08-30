package com.sjl.server.goods.service;

import api.domain.Goods;
import api.service.IGoodsService;
import com.sjl.rpc.context.annotation.SjlRpcService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: JianLei
 * @date: 2020/6/14 3:32 下午
 * @description:
 */

@SjlRpcService(IGoodsService.class)
public class GoodsServiceImpl implements IGoodsService {
  @Override
  public List<Goods> getGoodsById(Long id) {
    Goods goods = Goods.builder().goodsId(String.valueOf(id)).goodsName("iphone").price("4999").build();

    List<Goods> goodsList = new ArrayList<>();
    goodsList.add(goods);
    return goodsList;
  }

  @Override
  public String helloRpc(String var) {
    return "hello:"+var;
  }
}
