package com.github.rpc.context.config;

import com.github.rpc.context.constants.Constant;
import com.github.rpc.context.exception.RocketException;
import com.github.rpc.context.factory.RocketServiceFactory;
import com.github.rpc.context.remote.discover.registry.ZkServiceRegistry;
import com.github.rpc.context.spring.annotation.RocketReference;
import com.github.rpc.context.spring.annotation.RocketReferenceAttribute;
import com.github.rpc.context.util.SpringBeanUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jianlei.shi
 * @date 2021/3/23 6:55 下午
 * @description ReferenceConfigBean
 */
@Data
@Slf4j
public class ReferenceConfigBean<T> {

    private final Map<Class<?>, Object> CACHE_REFS = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();

    private Class<?> interfaces;

    private volatile T ref;

    private RocketReference rocketReference;

    private Environment environment;

    public ReferenceConfigBean(RocketReference rocketReference) {
        this.rocketReference = rocketReference;
    }

    public ReferenceConfigBean() {
    }

    public T get() {
        lock.lock();
        try {
            if (ref == null) {
                init();
            }
        } catch (Exception e) {
            log.error("ReferenceConfigBean init exception,",e);
            throw new RocketException("ReferenceConfigBean init exception");
        } finally {
            lock.unlock();
        }
        return ref;
    }

    private void init() {
        String protocol = environment.getProperty(Constant.PROTOCOL);
        doRefer();
        final Object proxy = buildRocketService(interfaces, protocol, rocketReference);
        if (proxy != null) {
            this.ref= (T) proxy;
        }

    }


    private void doRefer() {
        final ZkServiceRegistry registry = SpringBeanUtil.getBean(ZkServiceRegistry.class);
        String version = getVersion(rocketReference);
        registry.registry(interfaces, null, version, false);
    }

    private Object buildRocketService(Class<?> tyClass, String protocol, RocketReference reference) {
        return new RocketServiceFactory<>(tyClass, new RocketReferenceAttribute(
                tyClass, reference.version(), reference.group(), tyClass.getSimpleName(), protocol)).getObject();
    }

    private String getVersion(RocketReference reference) {
        return (String) AnnotationUtils.getAnnotationAttributes(reference).get(Constant.VERSION);
    }
}
