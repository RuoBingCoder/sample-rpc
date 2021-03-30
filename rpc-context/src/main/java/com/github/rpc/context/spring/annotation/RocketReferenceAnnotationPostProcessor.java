package com.github.rpc.context.spring.annotation;

import com.github.rpc.context.config.ReferenceConfigBean;
import com.github.rpc.context.factory.RocketServiceFactory;
import com.github.rpc.context.remote.discover.registry.ZkServiceRegistry;
import com.github.rpc.context.util.AnnotationUtil;
import com.github.rpc.context.util.ReflectUtils;
import com.github.rpc.context.constants.Constant;
import com.github.rpc.context.util.SpringBeanUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: JianLei
 * @date: 2020/9/13 11:45 上午
 * @description: 对代理重构, 使用BeanPostProcessor减少代码允许在bean初始化前后做处理
 */
@Component
@Slf4j
@DependsOn("springBeanUtil")
public class RocketReferenceAnnotationPostProcessor implements BeanPostProcessor, EnvironmentAware, BeanFactoryAware {

    private ConfigurableEnvironment environment;
    private BeanFactory beanFactory;
    private final Map<Class<?>, Object> CACHE_REFS = new ConcurrentHashMap<>();

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        doInjectRefBean(bean, beanName);
        return bean;
    }

    private void doInjectRefBean(Object bean, String beanName) {
        String protocol = environment.getProperty(Constant.PROTOCOL);
        Field[] fields = ReflectUtils.getFields(bean.getClass());
        for (Field field : fields) {
            if (field.isAnnotationPresent(RocketReference.class)) {
                if (field.getType().isInterface()) {
                    RocketReference reference = field.getAnnotation(RocketReference.class);
                    log.info("version is:{}====>group is:{} rocket.protocol:{}", reference.version(), reference.group(), protocol);
                    field.setAccessible(true);
                     try {
                         final Object ref = CACHE_REFS.get(field.getType());
                         if (ref != null) {
                             log.info("开始<走缓存>bean name:{}", beanName);
                             field.set(bean, CACHE_REFS.get(field.getType()));
                         } else {
                             log.info("开始<创建缓存> bean name:{}", beanName);
                             Object proxy = registryRefConfigBean(field.getType(), reference);
                             field.set(bean, proxy);
                             CACHE_REFS.putIfAbsent(field.getType(), proxy);
                         }
                     } catch (Exception e) {

                     }
                }
            }
        }
    }

    private Object registryRefConfigBean(Class<?> type, RocketReference reference) {
        if (beanFactory instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) this.beanFactory;
            ReferenceConfigBean referenceConfigBean = new ReferenceConfigBean();
            referenceConfigBean.setInterfaces(type);
            referenceConfigBean.setRocketReference(reference);
            referenceConfigBean.setEnvironment(environment);
            beanFactory.registerSingleton(type.getSimpleName(), referenceConfigBean);
            return referenceConfigBean.get();

        }
        return null;
    }




    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
