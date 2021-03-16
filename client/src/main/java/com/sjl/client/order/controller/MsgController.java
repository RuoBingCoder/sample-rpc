package com.sjl.client.order.controller;

import api.domain.Goods;
import api.service.IGoodsService;
import api.service.IProductService;
import com.google.gson.Gson;
import com.sjl.rpc.context.spring.annotation.RocketReference;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/msg")
@Slf4j
public class MsgController {
  @RocketReference(value = IGoodsService.class, version = "1.0.1")
  private IGoodsService iGoodsService;

  @RocketReference(value = IProductService.class, version = "1.0.1")
  private IProductService iProductService;

  //  @Autowired
  //  private RpcServiceTest rpcServiceTest;
  @GetMapping("/getOrder")
  public String getMsg() {
    List<Goods> goodsById = iGoodsService.getGoodsById(1000L);
    log.info("*******Goods is:{}", new Gson().toJson(goodsById));
    return "success";
  }

  @GetMapping("/hello")
  public String hello() {
    return iGoodsService.helloRpc("rpc");
  }

  @GetMapping(value = "/product", name = "product")
  public String product() {
    return iProductService.count(12L) + "";
  }
}
