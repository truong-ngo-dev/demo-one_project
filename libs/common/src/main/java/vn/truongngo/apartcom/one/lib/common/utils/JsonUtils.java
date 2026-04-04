package vn.truongngo.apartcom.one.lib.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * Json utility
 * @author Truong Ngo
 * @version 1.0.0
 * */
public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * Default object mapper
     * */
    private static final ObjectMapper MAPPER = new ObjectMapper();


    /**
     * Converts a JSON string into an object of the specified type {@code T}.
     *
     * @param json  The JSON string to be converted.
     * @param clazz The class type of {@code T}.
     * @param <T>   The target object type.
     * @return An object of type {@code T} parsed from the JSON string, or {@code null} if the input is {@code null}.
     * @throws IllegalArgumentException If the JSON string is invalid or cannot be deserialized.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return Objects.isNull(json) ? null : MAPPER.readValue(json, clazz);
        } catch (JacksonException e) {
            log.error("Failed to parse json string: {}, cause: {}", json, e.getMessage(), e);
            throw new IllegalArgumentException("Failed to parse json string: " + json + ", cause: " + e.getMessage(), e);
        }
    }


    /**
     * Converts a JSON string into an object of the specified parameterized type {@code T}.
     *
     * @param json The JSON string to be converted.
     * @param type The parameterized type reference of {@code T}.
     * @param <T>  The target object type.
     * @return An object of type {@code T} parsed from the JSON string, or {@code null} if the input is {@code null}.
     * @throws IllegalArgumentException If the JSON string is invalid or cannot be deserialized.
     */
    public static <T> T fromJson(String json, TypeReference<T> type) {
        try {
            return Objects.isNull(json) ? null : MAPPER.readValue(json, type);
        } catch (JacksonException e) {
            log.error("Failed to parse json string: {}, cause: {}", json, e.getMessage(), e);
            throw new IllegalArgumentException("Failed to parse json string: " + json + ", cause: " + e.getMessage(), e);
        }
    }


    /**
     * Serializes an object into a JSON string.
     *
     * @param obj The object to be serialized.
     * @return A JSON string representation of the object.
     * @throws IllegalArgumentException If serialization fails.
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JacksonException e) {
            log.error("Failed to stringify json object: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to stringify json object: " + e.getMessage(), e);
        }
    }
}
