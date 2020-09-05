package com.sjl.server.goods;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author: JianLei
 * @date: 2020/9/3 7:14 下午
 * @description:
 */

public class TestB extends TestA implements TestInterface {

  public static void main(String[] args) {
      Class<?>[] interfaces = TestB.class.getInterfaces();
    for (Class<?> iface : interfaces) {
      System.out.println(iface.getName());
    }
  }
}
