package com.github.rpc.context.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author jianlei.shi
 * @date 2021/1/9 10:00 下午
 * @description ReflectUtils
 */

public class ReflectUtils {


    public static Field[] getFields(Class<?> type) {
        if (type != null) {
            return type.getDeclaredFields();
        }
        throw new IllegalArgumentException("type is non null!");
    }

    public static <T> T getAnnotation(Field field, Class<? extends Annotation> annotationType) {
        return (T) field.getAnnotation(annotationType);

    }

    public static boolean isAnnotationType(Class<?> type, Class<? extends Annotation> annotationType) {
        Field[] fields = getFields(type);
        if (fields.length > 0) {
            for (Field field : fields) {
                return field.isAnnotationPresent(annotationType);
            }
        }
        return false;
    }
}
