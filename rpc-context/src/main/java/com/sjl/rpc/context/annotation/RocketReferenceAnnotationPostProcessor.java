package com.sjl.rpc.context.annotation;

import com.sjl.rpc.context.factory.RocketServiceFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author: JianLei
 * @date: 2020/9/13 11:45 上午
 * @description: 对代理重构,使用BeanPostProcessor减少代码允许在bean初始化前后做处理
 */
@Component
@Slf4j
public class RocketReferenceAnnotationPostProcessor implements BeanPostProcessor {


  @SneakyThrows
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    Field[] fields = bean.getClass().getDeclaredFields();
    for (Field field : fields) {
      if (field.isAnnotationPresent(RocketReference.class)) {
        if (field.getType().isInterface()) {
          RocketReference reference = field.getAnnotation(RocketReference.class);
          System.out.println(
              "version is:" + reference.version() + "====>" + "group is:" + reference.group());
          field.setAccessible(true);
          field.set(
              bean,
              new RocketServiceFactory<>(field.getType(), new RocketReferenceAttribute(
                      field.getType(), reference.version(), reference.group(), field.getType().getSimpleName())).getObject());
        }
      }
    }
    return bean;
  }


}
