package com.sjl.nettyserver.goods.util;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author: jianlei
 * @date: 2020/6/19
 * @description: PropertiesUtil
 */
public class PropertiesUtil {

  public static String getProperties(String name) throws IOException {
    final InputStream resourceAsStream =
        PropertiesUtil.class.getClassLoader().getResourceAsStream("zk.properties");
    Properties properties = new Properties();
    properties.load(resourceAsStream);
    return properties.getProperty(name) == null ? "--" : properties.getProperty(name);
  }

  public static Map<String, Object> getSjlAllConfigProperties(String name) throws IOException {

    Map<String, Object> configMap = new HashMap<>();

    final InputStream resourceAsStream =
            PropertiesUtil.class.getClassLoader().getResourceAsStream(name+".properties");
    Properties properties = new Properties();
    properties.load(resourceAsStream);
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {

      configMap.put((String) entry.getKey(), entry.getValue());
    }
    return configMap;
  }
  public static void main(String[] args) throws IOException {
    final Map<String, Object> configProperties = PropertiesUtil.getSjlAllConfigProperties(null);
    System.out.println(new Gson().toJson(configProperties));
  }

}
