package com.sjl.rpc.context.factory;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 为接口注入实现类
 */
@Slf4j
@Deprecated
public class RpcServiceBeanDefinitionRegistry implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, ApplicationContextAware {
    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private ApplicationContext applicationContext;
    private MetadataReaderFactory metadataReaderFactory;
    private ResourcePatternResolver resourcePatternResolver;
    public static String scannerPackages = "";



    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("--->>scannerPackages is :{}", JSONObject.toJSONString(scannerPackages));
        //这里主要用来做消费者接口扫描注入代理bean,服务端则不做处理,可能会有不足之处,只是做了简单处理
        if ("".equals(scannerPackages)){
            return;
        }

        Set<Class<?>> clazzSet = scannerPackages(scannerPackages);
        clazzSet.stream().filter(Class::isInterface).forEach(x -> registerBean(registry, x));
    }

    private void registerBean(BeanDefinitionRegistry registry, Class clazz) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.getConstructorArgumentValues().addGenericArgumentValue(clazz);
        definition.setBeanClass(RocketServiceFactory.class);
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
        registry.registerBeanDefinition(clazz.getSimpleName(), definition);
    }

    /**
     * 获取指定路径及子路径下的所有类
     * 源码定位:ClassPathScanningCandidateComponentProvider类
     */
    private Set<Class<?>> scannerPackages(String basePackage) {
        Set<Class<?>> set = new LinkedHashSet<>();
        //解析包名将com.sjl.test replace com/sjl/test
        String basePackageName = ClassUtils.convertClassNameToResourcePath(applicationContext.getEnvironment().resolveRequiredPlaceholders(basePackage));
        //组装类.class路径扫描
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                basePackageName + '/' + DEFAULT_RESOURCE_PATTERN;
        try {
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();
                    Class<?> clazz;
                    try {
                        clazz = Class.forName(className);
                        set.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return set;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * 源码定位:ClassPathScanningCandidateComponentProvider类中
     * @param resourceLoader
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}