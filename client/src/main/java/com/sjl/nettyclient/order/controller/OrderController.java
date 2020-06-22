package com.sjl.nettyclient.order.controller;

import api.domain.Goods;
import api.service.IGoodsService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: JianLei
 * @date: 2020/6/13 11:00 下午
 * @description:
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
  @Autowired private IGoodsService iGoodsService;

 @Value("${sjl.name}")
  private String name;

  @GetMapping("/getOrder")
  public String getOrder() {
    List<Goods> goodsById = iGoodsService.getGoodsById(1000L);
    log.info("*******Goods is:{}", new Gson().toJson(goodsById));
    return "success";
  }

  @GetMapping("/hello")
  public String hello() {
    return "hello"+name;
  }
}
