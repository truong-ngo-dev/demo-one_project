package vn.truongngo.apartcom.one.lib.abac.utils;

import java.lang.invoke.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import static org.springframework.util.StringUtils.capitalize;

/**
 * Reflection utility providing field access and nested object value resolution.
 * @author Truong Ngo
 */
public class ReflectionUtils {

    public static Optional<Field> getField(Class<?> clazz, String fieldName) {
        if (Objects.isNull(clazz) || Objects.isNull(fieldName)) {
            throw new IllegalArgumentException("Class and field name must not be null");
        }
        Class<?> currentClass = clazz;
        while (Objects.nonNull(currentClass) && !currentClass.equals(Object.class)) {
            try {
                return Optional.of(currentClass.getDeclaredField(fieldName));
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("all")
    public static Function getFieldAccessor(Class<?> clazz, String fieldName) {
        String getterName = clazz == boolean.class || clazz == Boolean.class
                ? "is" + capitalize(fieldName) : "get" + capitalize(fieldName);
        Method getterMethod;
        try {
            getterMethod = clazz.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The class (" + clazz + ") doesn't have the getter method ("
                    + getterName + ").", e);
        }
        Class<?> returnType = getterMethod.getReturnType();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        CallSite site;
        try {
            site = LambdaMetafactory.metafactory(lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    lookup.findVirtual(clazz, getterName, MethodType.methodType(returnType)),
                    MethodType.methodType(returnType, clazz));
        } catch (LambdaConversionException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("Lambda creation failed for method (" + getterMethod + ").", e);
        }
        try {
            return (Function) site.getTarget();
        } catch (Throwable e) {
            throw new IllegalArgumentException("Lambda creation failed for method (" + getterMethod + ").", e);
        }
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        if (Objects.isNull(obj) || Objects.isNull(fieldName)) {
            throw new IllegalArgumentException("Object and field name must not be null");
        }
        Class<?> clazz = obj.getClass();
        Field field = getField(clazz, fieldName).orElseThrow(
                () -> new IllegalArgumentException("Field " + fieldName + " not found"));
        field.setAccessible(true);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException("Unable to access field: " + fieldName);
        }
    }

    public static Object getObjectValue(Object obj, String path) {
        if (Objects.isNull(obj) || Objects.isNull(path)) {
            throw new IllegalArgumentException("Object and path must not be null");
        }
        String[] paths = path.split("\\.");
        Object target = obj;
        Object value = null;
        for (int i = 0; i < paths.length; i++) {
            String p = paths[i];
            if (Objects.isNull(target)) throw new IllegalArgumentException("Path '" + p + "' error");
            if (p.matches(".*\\[\\d+]$")) {
                String prefix = p.substring(0, p.length() - 3);
                int index = StringUtils.extractIndex(p);
                if (!prefix.isEmpty()) {
                    Object base = getValueOfNonCollectionObject(target, prefix);
                    if (Objects.isNull(base)) throw new IllegalArgumentException("Path '" + p + "' error");
                    value = getCollectionElement(p, index, base);
                } else {
                    value = getCollectionElement(p, index, target);
                }
            } else {
                value = getValueOfNonCollectionObject(target, p);
            }
            if (i != paths.length - 1) {
                if (value == null) throw new IllegalArgumentException("Path '" + p + "' error");
                else target = value;
            } else {
                target = value;
            }
        }
        return value;
    }

    private static Object getCollectionElement(String path, int index, Object base) {
        Object value;
        if (!TypeUtils.isCollection(base.getClass())) throw new IllegalArgumentException("Base object is not collection");
        if (Collection.class.isAssignableFrom(base.getClass())) {
            List<?> list = new ArrayList<>((Collection<?>) base);
            if (index < 0 || index >= list.size()) throw new IllegalArgumentException("Path " + path + " error");
            value = list.get(index);
        } else {
            if (index < 0 || index >= Array.getLength(base)) throw new IllegalArgumentException("Path " + path + " error");
            value = Array.get(base, index);
        }
        return value;
    }

    private static Object getValueOfNonCollectionObject(Object obj, String name) {
        if (Objects.isNull(obj) || Objects.isNull(name)) throw new IllegalArgumentException("Object and name must not be null");
        if (TypeUtils.isValueBased(obj.getClass())) throw new IllegalArgumentException("Object is value based type");
        if (TypeUtils.isCollection(obj.getClass())) throw new IllegalArgumentException("Object is collection");
        if (TypeUtils.isMap(obj.getClass())) return ((Map<?, ?>) obj).get(name);
        return getFieldValue(obj, name);
    }
}
