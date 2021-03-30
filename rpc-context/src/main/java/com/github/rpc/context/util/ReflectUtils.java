package com.github.rpc.context.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationType) {
        return field.getAnnotation(annotationType);

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

    public static void setAccessible(Object obj) {
        if (obj instanceof Field) {
            Field field = (Field) obj;
            field.setAccessible(true);
        } else if (obj instanceof Method) {
            Method method = (Method) obj;
            method.setAccessible(true);
        }
    }
}
