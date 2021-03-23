package com.github.rpc.context.util;

import java.io.IOException;
import java.util.Properties;

/**
 * @author: JianLei
 * @date: 2020/9/1 4:49 下午
 * @description: zk地址获取
 */

public class PropertiesUtil {

    public static String getZkAddr(String key) throws IOException {
        Properties properties=new Properties();
        properties.load(PropertiesUtil.class.getClassLoader().getResourceAsStream("zk.properties"));
      return properties.getProperty(key);

    }

  public static void main(String[] args) throws IOException {
    System.out.println(PropertiesUtil.getZkAddr("zk.addr"));
  }
}
