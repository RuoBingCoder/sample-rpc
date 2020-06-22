package com.sjl.nettyserver.goods.controller;

import com.sjl.nettyserver.goods.annotation.SjlAutowired;
import com.sjl.nettyserver.goods.pojo.Test;
import com.sjl.nettyserver.goods.util.SpringBeanUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: jianlei
 * @date: 2020/6/19
 * @description: TestController
 */
@RestController
@RequestMapping("/test")
public class TestController {

  @SjlAutowired private Test test;

  @GetMapping("/hello")
  public void hello() {
    SpringBeanUtil.doAutowired();
    System.out.println("====================>>>>>>>" + test.toString());
  }
}
