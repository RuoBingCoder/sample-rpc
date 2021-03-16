package com.sjl.rpc.context.spring.annotation;

import cn.hutool.core.util.ObjectUtil;
import com.sjl.rpc.context.constants.Constant;
import com.sjl.rpc.context.factory.RocketServiceFactory;
import com.sjl.rpc.context.util.ReflectUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
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
public class RocketReferenceAnnotationPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private ConfigurableEnvironment environment;
    private final Map<Class<?>, Object> CACHE_REFS = new ConcurrentHashMap<>();

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        String protocol = environment.getProperty(Constant.PROTOCOL);
        Field[] fields = ReflectUtils.getFields(bean.getClass());
        for (Field field : fields) {
            if (field.isAnnotationPresent(RocketReference.class)) {
                if (field.getType().isInterface()) {
                    RocketReference reference = field.getAnnotation(RocketReference.class);
                    log.info("version is:{}====>group is:{} rocket.protocol:{}", reference.version(), reference.group(), protocol);
                    field.setAccessible(true);
                    final Object ref = CACHE_REFS.get(field.getType());
                    if (ref != null) {
                        log.info("开始<走缓存>bean name:{}", beanName);
                        field.set(bean, CACHE_REFS.get(field.getType()));
                    } else {
                        log.info("开始<创建缓存> bean name:{}", beanName);
                        Object proxy = buildRocketService(field, protocol, reference);
                        field.set(
                                bean,
                                proxy);
                        CACHE_REFS.putIfAbsent(field.getType(), proxy);
                    }
                }
            }
        }
        return bean;
    }

    private Object buildRocketService(Field field, String protocol, RocketReference reference) {
        return new RocketServiceFactory<>(field.getType(), new RocketReferenceAttribute(
                field.getType(), reference.version(), reference.group(), field.getType().getSimpleName(), protocol)).getObject();
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
