package com.sjl.rpc.context.util;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
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


    public static AnnotationAttributes getMergedAttributes(AnnotatedElement annotatedElement,
                                                           Class<? extends Annotation> annotationType,
                                                           PropertyResolver propertyResolver,
                                                           boolean ignoreDefaultValue,
                                                           String... ignoreAttributeNames) {
        Annotation annotation = getMergedAnnotation(annotatedElement, annotationType);
        return annotation == null ? null : fromMap(getAttributes(annotation, propertyResolver, ignoreDefaultValue, ignoreAttributeNames));

    }


    public static Map<String, Object> getAttributes(Annotation annotation, PropertyResolver propertyResolver,
                                                    boolean ignoreDefaultValue, String... ignoreAttributeNames) {

        if (annotation == null) {
            return emptyMap();
        }

        Map<String, Object> attributes = getAnnotationAttributes(annotation);

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
}
