package vn.truongngo.apartcom.one.lib.common.domain.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import vn.truongngo.apartcom.one.lib.common.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    private static final Logger log = LoggerFactory.getLogger(RequestContext.class);

    /**
     * Cache object theo type cụ thể.
     * Dùng khi cần lưu/lấy aggregate hoặc entity đã load trong request.
     * Tránh query DB nhiều lần cho cùng một object.
     *
     * Ví dụ:
     *   setValue(Device.class, deviceId, device)
     *   getValue(Device.class, deviceId)
     */
    private final Map<Class<?>, Map<Object, Object>> typeCache = new HashMap<>();

    /**
     * Cache tập hợp theo tên logic.
     * Dùng khi cần nhóm nhiều object dưới một tên nghiệp vụ cụ thể.
     *
     * Ví dụ:
     *   setValue("activeDevices", userId, deviceList)
     *   getValue("activeDevices", userId)
     */
    private final Map<String, Map<Object, Object>> nameCache = new HashMap<>();

    /**
     * Key-value chung cho metadata của request.
     * Dùng cho flags, config, hoặc data không thuộc về type/group cụ thể.
     *
     * Ví dụ:
     *   setProperty("requestedBy", userId)
     *   setProperty("traceId", traceId)
     */
    private final Map<String, Object> properties = new HashMap<>();

    public static RequestContext create() {
        return new RequestContext();
    }

    // ───────────── Type Cache ─────────────

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getTypeCaches(Class<V> valueType) {
        return (Map<K, V>) typeCache.computeIfAbsent(valueType, k -> new HashMap<>());
    }

    public <K, V> V getValue(Class<V> type, K key) {
        return getTypeCaches(type).get(key);
    }

    public <K, V> void setValue(Class<V> type, K key, V value) {
        Map<K, V> map = getTypeCaches(type);
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    public boolean hasTypeCache(Class<?> type) {
        return !getTypeCaches(type).isEmpty();
    }

    // ───────────── Name Cache ─────────────

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getNameCaches(String cacheName) {
        return (Map<K, V>) nameCache.computeIfAbsent(cacheName, k -> new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    public <K, V> V getValue(String cacheName, K key) {
        return (V) getNameCaches(cacheName).get(key);
    }

    public <K, V> void setValue(String cacheName, K key, V value) {
        Map<K, V> map = getNameCaches(cacheName);
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    public boolean hasNameCache(String cacheName) {
        return !getNameCaches(cacheName).isEmpty();
    }

    // ───────────── Properties ─────────────

    public <T> T getProperty(String key, Class<T> type, T defaultValue) {
        Object value = properties.get(key);
        if (value == null) return defaultValue;
        try {
            return type.cast(value);
        } catch (ClassCastException e) {
            log.warn("Failed to cast property '{}' to type {}, returning default. Cause: {}",
                    key, type.getSimpleName(), e.getMessage());
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, TypeReference<T> type, T defaultValue) {
        Object value = properties.get(key);
        if (value == null) return defaultValue;
        try {
            return (T) value;
        } catch (ClassCastException e) {
            try {
                return JsonUtils.fromJson(JsonUtils.toJson(value), type);
            } catch (Exception ex) {
                log.warn("Failed to convert property '{}' to type {}, returning default. Cause: {}", key, type, ex.getMessage());
                return defaultValue;
            }
        }
    }

    public <T> T getProperty(String key, Class<T> type) {
        return getProperty(key, type, null);
    }

    public <T> T getProperty(String key, TypeReference<T> type) {
        return getProperty(key, type, null);
    }

    public <T> void setProperty(String key, T value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
    }

    // ───────────── Lifecycle ─────────────

    public void clear() {
        typeCache.clear();
        nameCache.clear();
        properties.clear();
    }
}