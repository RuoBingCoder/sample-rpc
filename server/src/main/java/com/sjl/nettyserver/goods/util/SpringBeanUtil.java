package com.sjl.nettyserver.goods.util;

import com.google.gson.Gson;
import com.sjl.nettyserver.goods.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
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
                bean.getClass().getAnnotation(RpcService.class).value().getName();

            handlerMap.put(interFaceName, bean);
          }
          return handlerMap;
        }
      }
    } catch (Exception e) {
      log.error("获取bean出现异常", e);
    }
    return new ConcurrentHashMap<>();
  }
}
