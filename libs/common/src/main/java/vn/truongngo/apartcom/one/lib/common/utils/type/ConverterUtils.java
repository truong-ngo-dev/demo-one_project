/*
 * Created by Truong Ngo (2026).
 */
package vn.truongngo.apartcom.one.lib.common.utils.type;

import vn.truongngo.apartcom.one.lib.common.utils.lang.DateUtils;
import vn.truongngo.apartcom.one.lib.common.utils.lang.NumberUtils;
import vn.truongngo.apartcom.one.lib.common.utils.reflect.ClassUtils;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Utility for type conversion.
 * <p>
 * This class acts as a central coordinator for converting between different data types.
 * It leverages {@link NumberUtils} and {@link DateUtils} for specialized conversions
 * and provides a registry for custom converters.
 * </p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 * <li><b>Number Conversion:</b> String to Number, Number to Number (with overflow checks).</li>
 * <li><b>Date Conversion:</b> String to Date/Time, Timestamp to Date/Time.</li>
 * <li><b>Logic Conversion:</b> String to Boolean (supports "true", "1", "y", "on").</li>
 * <li><b>Extensibility:</b> Register custom converters via {@link #register(Class, Class, Function)}.</li>
 * </ul>
 *
 * <p>This class is thread-safe and intended for static use.</p>
 *
 * @author Truong Ngo
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConverterUtils {

    // Use ConcurrentReferenceHashMap to optimize memory for Cache
    private static final Map<String, Function<Object, Object>> REGISTRY = Collections.synchronizedMap(new WeakHashMap<>(256));

    static {
        registerDefaultConverters();
    }

    /**
     * Prevents instantiation of this utility class.
     * @throws UnsupportedOperationException always
     */
    private ConverterUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /*
     * -----------------------------------------------------------------------------------------------------------------
     * INITIALIZATION
     * -----------------------------------------------------------------------------------------------------------------
     */

    private static void registerDefaultConverters() {
        // --- 1. Number Group (Leverage NumberUtils) ---
        // Allow smart String -> Number conversion
        register(String.class, Integer.class, s -> NumberUtils.parse(s, Integer.class));
        register(String.class, Long.class,    s -> NumberUtils.parse(s, Long.class));
        register(String.class, Float.class,    s -> NumberUtils.parse(s, Float.class));
        register(String.class, Double.class,  s -> NumberUtils.parse(s, Double.class));

        // Allow casting between Numbers (Integer -> Long, Double -> BigDecimal...)
        // NumberUtils.convertNumber handles overflow logic
        register(Number.class, Integer.class, n -> NumberUtils.convertNumber(n, Integer.class));
        register(Number.class, Long.class,    n -> NumberUtils.convertNumber(n, Long.class));

        // --- 2. Date Group (Leverage DateUtils) ---
        // Convert String to Java 8 Time formats via Heuristic Parsing
        register(String.class, java.time.LocalDate.class, DateUtils::parseLocalDate);
        register(String.class, java.time.LocalDateTime.class, DateUtils::parseLocalDateTime);
        register(String.class, java.util.Date.class, s -> DateUtils.parseDate(s, java.time.ZoneId.systemDefault()));

        // Convert Timestamp (Long) to Date/LocalDateTime
        register(Long.class, java.util.Date.class,          l -> DateUtils.parseAsMilliWithSystemZone(l, java.util.Date.class));
        register(Long.class, java.time.LocalDateTime.class, l -> DateUtils.parseAsMilliWithSystemZone(l, java.time.LocalDateTime.class));

        // --- 3. Logic Group ---
        register(String.class, Boolean.class, s -> {
            String val = s.trim().toLowerCase();
            return "true".equals(val) || "1".equals(val) || "y".equals(val) || "on".equals(val);
        });
    }

    /*
     * -----------------------------------------------------------------------------------------------------------------
     * CONVERSION LOGIC
     * -----------------------------------------------------------------------------------------------------------------
     */

    /**
     * Converts the source object to the target type.
     * <p>
     * This method attempts to find a registered converter for the source and target types.
     * It supports direct matching, inheritance-based matching, Enum conversion, and fallback to {@code toString()}.
     * </p>
     *
     * <pre>{@code
     * Integer i = ConverterUtils.convert("123", Integer.class); // 123
     * Boolean b = ConverterUtils.convert("on", Boolean.class);  // true
     * }</pre>
     *
     * @param <T>        the target type
     * @param source     the source object to convert (maybe {@code null})
     * @param targetType the target class type (must not be {@code null})
     * @return the converted object, or {@code null} if source is null
     * @throws IllegalArgumentException if no suitable converter is found
     * @since 1.0.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> T convert(Object source, Class<T> targetType) {
        if (source == null) return null;

        // If already the correct type, cast directly
        if (targetType.isInstance(source)) return targetType.cast(source);

        Class<?> sType = ClassUtils.resolvePrimitiveIfNecessary(source.getClass());
        Class<?> tType = ClassUtils.resolvePrimitiveIfNecessary(targetType);

        // 1. Find exact match in Registry
        Function<Object, Object> converter = REGISTRY.get(makeKey(sType, tType));

        // 2. If not found, try finding by parent-child relationship (e.g., Integer is a Number)
        if (converter == null) {
            converter = findCompatibleConverter(sType, tType);
        }

        if (converter != null) {
            return (T) converter.apply(source);
        }

        // 3. Special handling for Enum
        if (tType.isEnum() && source instanceof String) {
            return (T) Enum.valueOf((Class<Enum>) tType, (String) source);
        }

        // 4. Final fallback: use toString() if target is String
        if (tType == String.class) return (T) source.toString();

        throw new IllegalArgumentException("No converter registered for " + sType.getName() + " -> " + tType.getName());
    }

    /**
     * Registers a custom converter.
     *
     * <pre>{@code
     * ConverterUtils.register(String.class, MyType.class, s -> new MyType(s));
     * }</pre>
     *
     * @param <S>    the source type
     * @param <T>    the target type
     * @param source the source class (must not be {@code null})
     * @param target the target class (must not be {@code null})
     * @param func   the conversion function (must not be {@code null})
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public static <S, T> void register(Class<S> source, Class<T> target, Function<S, T> func) {
        REGISTRY.put(makeKey(source, target), (Function<Object, Object>) func);
    }

    /*
     * -----------------------------------------------------------------------------------------------------------------
     * INTERNAL HELPERS
     * -----------------------------------------------------------------------------------------------------------------
     */

    private static Function<Object, Object> findCompatibleConverter(Class<?> sType, Class<?> tType) {
        for (Map.Entry<String, Function<Object, Object>> entry : REGISTRY.entrySet()) {
            String[] parts = entry.getKey().split(":");
            try {
                Class<?> regSource = Class.forName(parts[0]);
                Class<?> regTarget = Class.forName(parts[1]);

                if (regSource.isAssignableFrom(sType) && tType.isAssignableFrom(regTarget)) {
                    return entry.getValue();
                }
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }

    private static String makeKey(Class<?> s, Class<?> t) {
        return s.getName() + ":" + t.getName();
    }
}
