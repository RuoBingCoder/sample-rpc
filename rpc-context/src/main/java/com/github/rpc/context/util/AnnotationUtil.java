package com.github.rpc.context.util;

import com.github.rpc.context.exception.RocketException;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.PropertyResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.core.annotation.AnnotationUtils.getDefaultValue;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ObjectUtils.containsElement;
import static org.springframework.util.ObjectUtils.nullSafeEquals;
import static org.springframework.util.StringUtils.trimWhitespace;

/**
 * @author: JianLei
 * @date: 2020/8/31 5:31 下午
 * @description:
 */

public class AnnotationUtil {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public static AnnotationAttributes getMergedAttributes(AnnotatedElement annotatedElement,
                                                           Class<? extends Annotation> annotationType,
                                                           PropertyResolver propertyResolver,
                                                           boolean ignoreDefaultValue,
                                                           String... ignoreAttributeNames) {
        Annotation annotation = getMergedAnnotation(annotatedElement, annotationType);
        return annotation == null ? null : fromMap(getAttributes(annotation, propertyResolver, ignoreDefaultValue, ignoreAttributeNames));

    }


    /**
     * 得到属性 copy spring
     *
     * @param annotation           注释
     * @param propertyResolver     属性解析器
     * @param ignoreDefaultValue   忽略默认值
     * @param ignoreAttributeNames 忽略属性名称
     * @return {@link Map<String, Object> }
     * @author jianlei.shi
     * @date 2021-03-21 16:07:20
     */
    public static Map<String, Object> getAttributes(Annotation annotation, PropertyResolver propertyResolver,
                                                    boolean ignoreDefaultValue, String... ignoreAttributeNames) {

        if (annotation == null) {
            return emptyMap();
        }

        Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);

        Map<String, Object> actualAttributes = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {

            String attributeName = entry.getKey();
            Object attributeValue = entry.getValue();

            // ignore default attribute value
            if (ignoreDefaultValue && nullSafeEquals(attributeValue, getDefaultValue(annotation, attributeName))) {
                continue;
            }

            /**
             * @since 2.7.1
             * ignore annotation member
             */
            if (attributeValue.getClass().isAnnotation()) {
                continue;
            }
            if (attributeValue.getClass().isArray() && attributeValue.getClass().getComponentType().isAnnotation()) {
                continue;
            }
            actualAttributes.put(attributeName, attributeValue);
        }


        return resolvePlaceholders(actualAttributes, propertyResolver, ignoreAttributeNames);
    }

    public static Map<String, Object> resolvePlaceholders(Map<String, Object> sourceAnnotationAttributes,
                                                          PropertyResolver propertyResolver,
                                                          String... ignoreAttributeNames) {

        if (isEmpty(sourceAnnotationAttributes)) {
            return emptyMap();
        }

        Map<String, Object> resolvedAnnotationAttributes = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : sourceAnnotationAttributes.entrySet()) {

            String attributeName = entry.getKey();

            // ignore attribute name to skip
            if (containsElement(ignoreAttributeNames, attributeName)) {
                continue;
            }

            Object attributeValue = entry.getValue();

            if (attributeValue instanceof String) {
                attributeValue = resolvePlaceholders(valueOf(attributeValue), propertyResolver);
            } else if (attributeValue instanceof String[]) {
                String[] values = (String[]) attributeValue;
                for (int i = 0; i < values.length; i++) {
                    values[i] = resolvePlaceholders(values[i], propertyResolver);
                }
                attributeValue = values;
            }

            resolvedAnnotationAttributes.put(attributeName, attributeValue);
        }

        return unmodifiableMap(resolvedAnnotationAttributes);
    }

    private static String resolvePlaceholders(String attributeValue, PropertyResolver propertyResolver) {
        String resolvedValue = attributeValue;
        if (propertyResolver != null) {
            resolvedValue = propertyResolver.resolvePlaceholders(resolvedValue);
            resolvedValue = trimWhitespace(resolvedValue);
        }
        return resolvedValue;
    }


    /**
     * 自定义 获取注解属性
     *
     * @param field          场
     * @param classes        类
     * @param annotationType 注释类型
     * @return {@link Map<String, Object> }
     * @author jianlei.shi
     * @date 2021-03-21 16:07:36
     */
    public static Map<String, Object> getAnnotationAttributes(Object obj, Class<? extends Annotation> annotationType) {
        Annotation[] annotations = null;
        if (obj instanceof Field){
            Field field= (Field) obj;
            field.setAccessible(true);
            annotations = field.getDeclaredAnnotations();
        } else if (obj instanceof Class){
            Class<?> clazz= (Class<?>) obj;
            annotations = clazz.getAnnotations();
        }
        Map<String, Object> annotationAttributes = new HashMap<>();
        assert annotations != null;
        if (annotations.length > 0) {
            for (java.lang.annotation.Annotation annotation : annotations) {
                if (annotation.annotationType() == annotationType) {
                    putAttrToMap(annotation, annotationAttributes);
                }
            }
        }
        return annotationAttributes;
    }

    private static void putAttrToMap(java.lang.annotation.Annotation target, Map<String, Object> annotationAttributes) {
        final Method[] methods = target.annotationType().getDeclaredMethods();
        if (methods.length > 0){
            for (Method method : methods) {
                if ("toString".equals(method.getName())|| "hashCode".equals(method.getName())){
                    continue;
                }
                try {
                    final Object res = method.invoke(target, EMPTY_OBJECT_ARRAY);
                    annotationAttributes.put(method.getName(),res);
                } catch (Exception e) {
                    throw new RocketException("putAttrToMap error  "+e.getMessage());
                }

            }
        }
    }
}
