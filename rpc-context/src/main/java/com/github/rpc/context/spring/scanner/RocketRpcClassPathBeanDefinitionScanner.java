package com.github.rpc.context.spring.scanner;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.rpc.context.config.ServiceConfigBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.lang.annotation.Annotation;
import java.util.Set;

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
    private BeanDefinitionRegistry registry;

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
        this.registry = registry;
    }

    /**
     * @return
     * @Author jianlei.shi
     * @Description 扫描远程调用服务接口类注册到ioc中
     * @Date 10:30 下午 2020/8/30
     * @Param basePackages
     **/
    @Override
    public int scan(String... basePackages) {
        if (log.isInfoEnabled()) {
            log.info("=======>>>basePackages :{}<<========", JSONObject.toJSONString(basePackages));
        }
        //确保先注册服务在注册configBean
        final Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        doRegisterServiceConfigBean(beanDefinitionHolders);
        return super.scan(basePackages);
    }

    /**
     * 服务配置bean
     *
     * @param beanDefinitionHolders bean定义持有者
     * @return
     * @author jianlei.shi
     * @date 2021-03-23 18:06:36
     */
    private void doRegisterServiceConfigBean(Set<BeanDefinitionHolder> beanDefinitionHolders) {
        if (CollectionUtil.isNotEmpty(beanDefinitionHolders)) {
            for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(ServiceConfigBean.class);
                beanDefinitionBuilder.addConstructorArgReference(beanDefinitionHolder.getBeanName());
                registry.registerBeanDefinition(generateServiceBeanName(ServiceConfigBean.class, beanDefinitionHolder.getBeanName()), beanDefinitionBuilder.getBeanDefinition());
            }
        }

    }

    private String generateServiceBeanName(Class<?> clazz, String beanName) {
        if (clazz != null) {
            return clazz.getSimpleName() + ":" + beanName;
        }
        return null;
    }

}
