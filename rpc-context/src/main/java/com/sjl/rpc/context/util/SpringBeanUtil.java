package com.sjl.rpc.context.util;

import com.google.gson.Gson;
import com.sjl.rpc.context.annotation.SjlRpcService;
import com.sjl.rpc.context.zk.ZkConnect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.net.InterfaceAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: JianLei
 * @date: 2020/6/14 3:44 下午
 * @description:
 */
@Component
@Slf4j
public class SpringBeanUtil implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SpringBeanUtil.applicationContext = applicationContext;
  }


  public static ConcurrentHashMap<String, Object> getBeansByAnnotation(Class<? extends Annotation>  clazz) {
    ConcurrentHashMap<String, Object> handlerMap = new ConcurrentHashMap<>();
    try {
      if (clazz != null) {
        Map beans = SpringBeanUtil.applicationContext.getBeansWithAnnotation(clazz);
        log.info("========beans 是:{}",new Gson().toJson(beans));

        if (beans.size() > 0 && beans != null) {
          for (Object bean : beans.values()) {
            String interFaceName =
                bean.getClass().getAnnotation(SjlRpcService.class).value().getName();

            handlerMap.put(interFaceName, bean);
          }
          //注册到zk上
//          ZkConnect.instance().create().withMode("/rocket/service",new InterfaceAddress())
          return handlerMap;
        }
      }
    } catch (Exception e) {
      log.error("获取bean出现异常", e);
    }
    return new ConcurrentHashMap<>();
  }

  public static void main(String[] args) throws ClassNotFoundException {
    Class<?> aClass = Class.forName("api.service.IGoodsService");
    System.out.println(aClass.getName());
  }
}
