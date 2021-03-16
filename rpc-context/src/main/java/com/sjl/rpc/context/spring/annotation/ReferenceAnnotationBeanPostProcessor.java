package com.sjl.rpc.context.spring.annotation;

import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.util.AnnotationUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: JianLei
 * @date: 2020/8/31 5:03 下午
 * @description: 主要是获取自定义注解信息,仿照@Autowired原理
 */
//@Component
@Slf4j
@AllArgsConstructor
@Deprecated
public class ReferenceAnnotationBeanPostProcessor
    extends InstantiationAwareBeanPostProcessorAdapter
    implements MergedBeanDefinitionPostProcessor,
        PriorityOrdered,
        BeanFactoryAware,
        BeanClassLoaderAware,
        EnvironmentAware,
        DisposableBean {
  private java.lang.Class[] annotationTypes;
  private Environment environment;
  private ClassLoader classLoader;
  private static final int CACHE_SIZE = Integer.getInteger("", 32);
  private final ConcurrentMap<
          String, ReferenceAnnotationBeanPostProcessor.AnnotatedInjectionMetadata>
      injectionMetadataCache =
          new ConcurrentHashMap<>(
                  CACHE_SIZE);

  private ConfigurableListableBeanFactory listableBeanFactory;

  public Environment getEnvironment() {
    return environment;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public ReferenceAnnotationBeanPostProcessor() {

    this.annotationTypes =
        new Class[] {RocketReference.class, RocketReference.class};
  }

  public Class<? extends Annotation>[] getAnnotationTypes() {
    return annotationTypes;
  }

  public void setAnnotationTypes(Class<? extends Annotation>[] annotationTypes) {
    this.annotationTypes = annotationTypes;
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.classLoader=classLoader;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    org.springframework.beans.factory.config.ConfigurableListableBeanFactory listableBeanFactory = (ConfigurableListableBeanFactory) beanFactory;
  }

  @Override
  public void destroy() throws Exception {
    Constant.CACHE_SERVICE_ATTRIBUTES_MAP.clear();
    injectionMetadataCache.clear();

  }

  @Override
  public void postProcessMergedBeanDefinition(
      RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment=environment;
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @SneakyThrows
  @Override
  public PropertyValues postProcessPropertyValues(
      PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName)
      throws BeansException {
    findInjectionMetadata(beanName, bean.getClass(), pvs);
    injectionMetadataCache.forEach(
        (s, aim) -> aim
            .getFieldElements()
            .forEach(
                afe -> Constant.CACHE_SERVICE_ATTRIBUTES_MAP.put(afe.field.getType().getName(),afe.attributes)));
    return pvs;
  }
  private void findInjectionMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
    // 退回类名称作为缓存键，以实现与自定义调用程序的向后兼容性。
    String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
    // 首先以最小的锁定快速检查并发映射.
    ReferenceAnnotationBeanPostProcessor.AnnotatedInjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
    if (InjectionMetadata.needsRefresh(metadata, clazz)) {
      synchronized (this.injectionMetadataCache) {
        metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
          if (metadata != null) {
            metadata.clear(pvs);
          }
          try {
            metadata = buildAnnotatedMetadata(clazz);
            this.injectionMetadataCache.put(cacheKey, metadata);
          } catch (NoClassDefFoundError err) {
            throw new IllegalStateException("Failed to introspect object class [" + clazz.getName() +
                    "] for annotation metadata: could not find class that it depends on", err);
          }
        }
      }
    }
  }
  public static class AnnotatedFieldElement extends InjectionMetadata.InjectedElement {

    private final Field field;

    private final AnnotationAttributes attributes;

    private volatile Object bean;

    public AnnotatedFieldElement(Field field, AnnotationAttributes attributes) {
      super(field, null);
      this.field = field;
      this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      AnnotatedFieldElement that = (AnnotatedFieldElement) o;
      return Objects.equals(field, that.field) &&
              Objects.equals(attributes, that.attributes) &&
              Objects.equals(bean, that.bean);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), field, attributes, bean);
    }
  }

  

  public List<ReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> findFieldAnnotationMetadata(
       Class<?> clazz) {
    final List<ReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> elements =
        new LinkedList<>();

    ReflectionUtils.doWithFields(
        clazz,
        field -> {
          for (Class<? extends Annotation> annotationType : this.getAnnotationTypes()) {

            AnnotationAttributes attributes =
                AnnotationUtil.getMergedAttributes(field, annotationType, getEnvironment(), true);
            if (attributes != null) {

              if (Modifier.isStatic(field.getModifiers())) {
                if (log.isWarnEnabled()) {
                  log.warn(
                      "@"
                          + annotationType.getName()
                          + " is not supported on static fields: "
                          + field);
                }
                return;
              }

              elements.add(new AnnotatedFieldElement(field, attributes));
            }
          }
        });
    return elements;
  }



  private ReferenceAnnotationBeanPostProcessor.AnnotatedInjectionMetadata buildAnnotatedMetadata(final Class<?> beanClass) {
    Collection<ReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> fieldElements = findFieldAnnotationMetadata(beanClass);
    return new AnnotatedInjectionMetadata(beanClass, fieldElements);

  }

  private static class AnnotatedInjectionMetadata extends InjectionMetadata {

    private final Collection<ReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> fieldElements;


    public AnnotatedInjectionMetadata(Class<?> targetClass, Collection<ReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> fieldElements) {
      super(targetClass, combine(fieldElements));
      this.fieldElements = fieldElements;
    }

    public Collection<ReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> getFieldElements() {
      return fieldElements;
    }



  }

  @SafeVarargs
  private static <T> Collection<T> combine(Collection<? extends T>... elements) {
    List<T> allElements = new ArrayList<>();
    for (Collection<? extends T> e : elements) {
      allElements.addAll(e);
    }
    return allElements;
  }

}
