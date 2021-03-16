package com.sjl.rpc.context.spring.scanner;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.factory.RpcServiceBeanDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: JianLei
 * @date: 2020/8/30 2:24 下午
 * @description: 对标有sjlRpcService注解的类进行注册到ioc中
 */
@Slf4j
public class RocketRpcClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

  private String basePackage;
  private String beanName;
  private Class<? extends Annotation> annotationClass;
  private String type;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBasePackage() {
    return basePackage;
  }

  public void setBasePackage(String basePackage) {
    this.basePackage = basePackage;
  }

  public String getBeanName() {
    return beanName;
  }

  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  public Class<? extends Annotation> getAnnotationClass() {
    return annotationClass;
  }

  public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
    this.annotationClass = annotationClass;
  }

  public RocketRpcClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
    super(registry);
  }
/**
 * @Author jianlei.shi
 * @Description 扫描远程调用服务接口类注册到ioc中
 * @Date 10:30 下午 2020/8/30
 * @Param basePackages 
 * @return
 **/
  @Override
  public int scan(String... basePackages) {
    if (log.isInfoEnabled()) {
      log.info("=======>>>basePackages :{}<<========", JSONObject.toJSONString(basePackages));
    }
    return super.scan(basePackages);
  }
}
