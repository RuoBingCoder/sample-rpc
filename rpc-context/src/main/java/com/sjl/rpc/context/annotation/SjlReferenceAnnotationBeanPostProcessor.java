package com.sjl.rpc.context.annotation;

import com.alibaba.fastjson.JSONObject;
import com.sjl.rpc.context.util.AnnotationUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import sun.reflect.annotation.AnnotationType;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: JianLei
 * @date: 2020/8/31 5:03 下午
 * @description:
 */
@Component
@Slf4j
@AllArgsConstructor
public class SjlReferenceAnnotationBeanPostProcessor
    extends InstantiationAwareBeanPostProcessorAdapter
    implements MergedBeanDefinitionPostProcessor,
        PriorityOrdered,
        BeanFactoryAware,
        BeanClassLoaderAware,
        EnvironmentAware,
        DisposableBean {
  private Class<? extends Annotation>[] annotationTypes = null;
  private Environment environment;
  private static final int CACHE_SIZE = Integer.getInteger("", 32);
  private final ConcurrentMap<
          String, SjlReferenceAnnotationBeanPostProcessor.AnnotatedInjectionMetadata>
      injectionMetadataCache =
          new ConcurrentHashMap<>(
                  CACHE_SIZE);

  public Environment getEnvironment() {
    return environment;
  }

  public SjlReferenceAnnotationBeanPostProcessor() {

    this.annotationTypes =
        new Class[] {SjlReference.class, com.sjl.rpc.context.annotation.SjlReference.class};
  }

  public Class<? extends Annotation>[] getAnnotationTypes() {
    return annotationTypes;
  }

  public void setAnnotationTypes(Class<? extends Annotation>[] annotationTypes) {
    this.annotationTypes = annotationTypes;
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {}

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {}

  @Override
  public void destroy() throws Exception {}

  @Override
  public void postProcessMergedBeanDefinition(
      RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {}

  @Override
  public void setEnvironment(Environment environment) {}

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public PropertyValues postProcessPropertyValues(
      PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName)
      throws BeansException {


    InjectionMetadata metadata = findInjectionMetadata(beanName, bean.getClass(), pvs);
    log.info("********************metadata is:{}",JSONObject.toJSONString(metadata.getClass().getName()));

    //    return super.postProcessPropertyValues(pvs, pds, bean, beanName);
    return pvs;
  }
  private InjectionMetadata findInjectionMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
    // Fall back to class name as cache key, for backwards compatibility with custom callers.
    String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
    // Quick check on the concurrent map first, with minimal locking.
    SjlReferenceAnnotationBeanPostProcessor.AnnotatedInjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
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
    return metadata;
  }
  public class AnnotatedFieldElement extends InjectionMetadata.InjectedElement {

    private final Field field;

    private final AnnotationAttributes attributes;

    private volatile Object bean;

    public AnnotatedFieldElement(Field field, AnnotationAttributes attributes) {
      super(field, null);
      this.field = field;
      this.attributes = attributes;
    }
  }

  

  public List<SjlReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> findFieldAnnotationMetadata(
       Class<?> clazz) {
    final List<SjlReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> elements =
        new LinkedList<>();

    ReflectionUtils.doWithFields(
        clazz,
        field -> {
          for (Class<? extends Annotation> annotationType : this.getAnnotationTypes()) {

            AnnotationAttributes attributes =
                AnnotationUtil.getMergedAttributes(field, annotationType, getEnvironment(), true);
            log.info(
                "=====attributes is:{} field is:{} annotationType is:{} ",
                JSONObject.toJSONString(attributes),
                field.getName(),
                annotationType.getName());
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



  private SjlReferenceAnnotationBeanPostProcessor.AnnotatedInjectionMetadata buildAnnotatedMetadata(final Class<?> beanClass) {
    Collection<SjlReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> fieldElements = findFieldAnnotationMetadata(beanClass);
    return new SjlReferenceAnnotationBeanPostProcessor.AnnotatedInjectionMetadata(beanClass, fieldElements);

  }

  private class AnnotatedInjectionMetadata extends InjectionMetadata {

    private final Collection<SjlReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> fieldElements;


    public AnnotatedInjectionMetadata(Class<?> targetClass, Collection<SjlReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> fieldElements) {
      super(targetClass, combine(fieldElements));
      this.fieldElements = fieldElements;
    }

    public Collection<SjlReferenceAnnotationBeanPostProcessor.AnnotatedFieldElement> getFieldElements() {
      return fieldElements;
    }



  }

  private static <T> Collection<T> combine(Collection<? extends T>... elements) {
    List<T> allElements = new ArrayList<>();
    for (Collection<? extends T> e : elements) {
      allElements.addAll(e);
    }
    return allElements;
  }

}
