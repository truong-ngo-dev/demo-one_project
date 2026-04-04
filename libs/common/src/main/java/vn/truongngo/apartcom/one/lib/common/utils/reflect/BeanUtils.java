package vn.truongngo.apartcom.one.lib.common.utils.reflect;

import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.lib.common.utils.type.ConverterUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Utility for Java Beans manipulation.
 * Kết hợp tốc độ của Spring (Cache PD) và sự linh hoạt của Apache (Populate, Nested).
 * * @author Truong Ngo
 */
public class BeanUtils {

    // Cache PropertyDescriptor để tránh quét lại Class (Tăng tốc gấp 10-50 lần)
    private static final Map<Class<?>, PropertyDescriptor[]> PD_CACHE = Collections.synchronizedMap(new WeakHashMap<>(256));

    private BeanUtils() {
        throw new UnsupportedOperationException("Cannot be instantiated!");
    }

    // ===================================================================================
    // 1. Nhóm KHỞI TẠO (Instantiation)
    // ===================================================================================

    public static <T> T instantiateClass(Class<T> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Cannot instantiate an interface: " + clazz.getName());
        }
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ReflectionUtils.makeAccessible(ctor); // Tận dụng ReflectionUtils của bạn
            return ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate [" + clazz.getName() + "]", e);
        }
    }

    // ===================================================================================
    // 2. Nhóm METADATA & CACHING
    // ===================================================================================

    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return PD_CACHE.computeIfAbsent(clazz, k -> {
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(k);
                return beanInfo.getPropertyDescriptors();
            } catch (Exception e) {
                throw new RuntimeException("Could not get PropertyDescriptors for " + k.getName(), e);
            }
        });
    }

    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {
        for (PropertyDescriptor pd : getPropertyDescriptors(clazz)) {
            if (pd.getName().equals(propertyName)) return pd;
        }
        return null;
    }

    // ===================================================================================
    // 3. Nhóm THAO TÁC THUỘC TÍNH (Property Manipulation)
    // ===================================================================================

    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        copyProperties(source, target, false, ignoreProperties);
    }

    public static void copyProperties(Object source, Object target, boolean ignoreNull, String... ignoreProperties) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : Collections.emptyList();
        PropertyDescriptor[] targetPds = getPropertyDescriptors(target.getClass());
        PropertyDescriptor[] sourcePds = getPropertyDescriptors(source.getClass());

        for (PropertyDescriptor targetPd : targetPds) {
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod == null || ignoreList.contains(targetPd.getName())) continue;

            PropertyDescriptor sourcePd = findPropertyDescriptor(sourcePds, targetPd.getName());
            if (sourcePd == null || sourcePd.getReadMethod() == null) continue;

            try {
                Object value = sourcePd.getReadMethod().invoke(source);
                if (value == null && ignoreNull) continue;

                setPropertyValue(target, targetPd, value);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Đổ dữ liệu từ Map vào Bean (Rất hữu ích cho RPA đọc Excel/JSON).
     */
    public static void populate(Object target, Map<String, ?> map) {
        if (target == null || map == null) return;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            PropertyDescriptor pd = getPropertyDescriptor(target.getClass(), entry.getKey());
            if (pd != null) {
                setPropertyValue(target, pd, entry.getValue());
            }
        }
    }

    /**
     * Chuyển Bean thành Map (Ngược lại với populate).
     */
    public static Map<String, Object> describe(Object bean) {
        if (bean == null) return Collections.emptyMap();
        Map<String, Object> map = new HashMap<>();
        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
            if (pd.getReadMethod() != null && !"class".equals(pd.getName())) {
                try {
                    map.put(pd.getName(), pd.getReadMethod().invoke(bean));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    // ===================================================================================
    // 4. Nhóm NÂNG CAO (Nested Properties)
    // ===================================================================================

    /**
     * Lấy giá trị sâu (vd: "ticket.customer.name").
     */
    public static Object getNestedProperty(Object bean, String expression) {
        if (bean == null || expression == null) return null;
        String[] fields = expression.split("\\.");
        Object current = bean;
        for (String field : fields) {
            if (current == null) return null;
            PropertyDescriptor pd = getPropertyDescriptor(current.getClass(), field);
            if (pd == null || pd.getReadMethod() == null) return null;
            try {
                current = pd.getReadMethod().invoke(current);
            } catch (Exception e) { return null; }
        }
        return current;
    }

    // ===================================================================================
    // HELPERS
    // ===================================================================================

    private static void setPropertyValue(Object target, PropertyDescriptor pd, Object value) {
        Method writeMethod = pd.getWriteMethod();
        if (writeMethod == null) return;
        try {
            Class<?> targetType = pd.getPropertyType();
            Object convertedValue = value;

            // ĐIỂM KHÁC BIỆT: Tự động gọi Converter nếu lệch kiểu dữ liệu
            if (value != null && !targetType.isAssignableFrom(value.getClass())) {
                convertedValue = ConverterUtils.convert(value, targetType);
            }

            ReflectionUtils.makeAccessible(writeMethod);
            writeMethod.invoke(target, convertedValue);
        } catch (Exception ignored) {}
    }

    private static PropertyDescriptor findPropertyDescriptor(PropertyDescriptor[] pds, String name) {
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals(name)) return pd;
        }
        return null;
    }
}