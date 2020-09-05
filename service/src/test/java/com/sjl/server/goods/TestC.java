package com.sjl.server.goods;

/**
 * @author: JianLei
 * @date: 2020/9/3 7:14 下午
 * @description:
 */

public class TestC  extends TestB {

  public static void main(String[] args) {
      String name = TestC.class.getSuperclass().getName();
    System.out.println("**"+TestC.class.getClassLoader());
    //TestC.class.getClassLoader().getParent() 父类加载器是ExClassLoader ->BootstrapClassLoader 不可见
    System.out.println("**"+TestC.class.getClassLoader().getParent().getParent());
    System.out.println("--"+Thread.currentThread().getContextClassLoader());
     // Thread类加载器为 null 也就是 BootstrapClassLoader 对Java不可见
    System.out.println("=="+Thread.currentThread().getClass().getClassLoader());
    System.out.println(name);
  }
}
