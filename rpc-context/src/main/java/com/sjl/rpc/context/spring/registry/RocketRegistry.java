package com.sjl.rpc.context.spring.registry;

import com.sjl.rpc.context.annotation.EnableRocketScan;
import com.sjl.rpc.context.annotation.RocketService;
import com.sjl.rpc.context.spring.scanner.RocketScanner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: JianLei
 * @date: 2020/8/30 2:27 下午
 * @description:
 *     <p>负责对标有@RpcService注解的bean进行注册到Spring ioc中,有些地方可能还不够完善后续待完善
 */
public class RocketRegistry
    implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, ApplicationContextAware {

  private ApplicationContext applicationContext;
  private ResourceLoader resourceLoader;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void registerBeanDefinitions(
      AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
    AnnotationAttributes mapperScanAttrs =
        AnnotationAttributes.fromMap(
            annotationMetadata.getAnnotationAttributes(EnableRocketScan.class.getName()));
    if (mapperScanAttrs != null) {
      registerBeanDefinitions(
          mapperScanAttrs, registry, generateBaseBeanName(annotationMetadata, 0));
    }
  }

  private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
    return importingClassMetadata.getClassName()
        + "#"
        + RocketScanner.class.getSimpleName()
        + "#"
        + index;
  }

  private void registerBeanDefinitions(
      AnnotationAttributes attributes, BeanDefinitionRegistry registry, String beanName) {
    RocketScanner scanner = new RocketScanner(registry);
    Class<? extends Annotation> annotationClass = attributes.getClass("annotationClass");
    if (!Annotation.class.equals(annotationClass)) {
      scanner.setAnnotationClass(annotationClass);
    }
    List<String> basePackages = new ArrayList<>();
    basePackages.addAll(
        Arrays.stream(attributes.getStringArray("value"))
            .filter(StringUtils::hasText)
            .collect(Collectors.toList()));

    basePackages.addAll(
        Arrays.stream(attributes.getStringArray("basePackages"))
            .filter(StringUtils::hasText)
            .collect(Collectors.toList()));

    basePackages.addAll(
        Arrays.stream(attributes.getClassArray("basePackagesClasses"))
            .map(ClassUtils::getPackageName)
            .collect(Collectors.toList()));
    basePackages.add(attributes.getString("type"));
    scanner.setBasePackage(StringUtils.collectionToCommaDelimitedString(basePackages));

    // 对标有SjlRpcService注解的类进行注册条件过滤
    scanner.addIncludeFilter(new AnnotationTypeFilter(RocketService.class));
    scanner.scan(StringUtils.toStringArray(basePackages));
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }
}
