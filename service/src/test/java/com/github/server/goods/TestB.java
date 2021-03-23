package com.github.server.goods;

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
