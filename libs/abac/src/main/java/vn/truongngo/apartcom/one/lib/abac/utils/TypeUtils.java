package vn.truongngo.apartcom.one.lib.abac.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Utility for type
 * @author Truong Ngo
 */
public class TypeUtils {

    private static final List<Class<?>> WRAPPER_TYPES = List.of(
            Boolean.class, Character.class, Byte.class,
            Short.class, Integer.class, Long.class,
            Float.class, Double.class, Void.class
    );

    public static boolean isWrapper(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive();
    }

    public static boolean isNumber(Class<?> clazz) {
        return isPrimitiveNumber(clazz) || isWrapperNumber(clazz);
    }

    private static boolean isPrimitiveNumber(Class<?> clazz) {
        return clazz == byte.class || clazz == short.class || clazz == int.class ||
               clazz == long.class || clazz == double.class || clazz == float.class;
    }

    private static boolean isWrapperNumber(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz);
    }

    public static boolean isInteger(Class<?> clazz) {
        return clazz == int.class || clazz == Integer.class ||
               clazz == long.class || clazz == Long.class ||
               clazz == short.class || clazz == Short.class ||
               clazz == byte.class || clazz == Byte.class ||
               clazz == BigInteger.class;
    }

    public static boolean isDecimal(Class<?> clazz) {
        return clazz == float.class || clazz == Float.class ||
               clazz == double.class || clazz == Double.class ||
               clazz == BigDecimal.class;
    }

    public static boolean isBoolean(Class<?> clazz) {
        return clazz == boolean.class || clazz == Boolean.class;
    }

    public static boolean isString(Class<?> clazz) {
        return clazz == String.class || clazz == Character.class || clazz == char.class;
    }

    public static boolean isDate(Class<?> clazz) {
        return clazz == LocalDate.class || clazz == LocalDateTime.class ||
               clazz == ZonedDateTime.class || clazz == Instant.class || clazz == Date.class;
    }

    public static boolean isEnum(Class<?> clazz) {
        return clazz.isEnum();
    }

    public static boolean isValueBased(Class<?> clazz) {
        return isNumber(clazz) || isBoolean(clazz) || isString(clazz) || isDate(clazz) || isEnum(clazz);
    }

    public static boolean isCollection(Class<?> clazz) {
        return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
    }

    public static boolean isMap(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    public static Class<?> getElementType(Class<?> clazz, Field field) {
        if (clazz.isArray()) {
            return clazz.getComponentType();
        } else {
            field.setAccessible(true);
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
    }

    public static <A extends Annotation> Class<?> getParameterGenericElementType(Method method, Class<A> annotationClass) {
        Parameter parameter = null;
        Parameter[] parameters = method.getParameters();
        for (Parameter p : parameters) {
            if (p.isAnnotationPresent(annotationClass)) {
                parameter = p;
            }
        }
        if (Objects.nonNull(parameter)) {
            if (parameter.getType().isArray()) {
                return parameter.getType().getComponentType();
            }
            if (Collection.class.isAssignableFrom(parameter.getType())) {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        return null;
    }

    public static Collection<?> getCollection(Object o) {
        if (Objects.isNull(o)) return null;
        if (o.getClass().isArray()) {
            List<Object> rs = new ArrayList<>();
            for (int i = 0; i < Array.getLength(o); i++) {
                rs.add(Array.get(o, i));
            }
            return rs;
        }
        if (Collection.class.isAssignableFrom(o.getClass())) {
            return (Collection<?>) o;
        }
        throw new IllegalArgumentException(o.getClass() + " is not a collection");
    }
}
