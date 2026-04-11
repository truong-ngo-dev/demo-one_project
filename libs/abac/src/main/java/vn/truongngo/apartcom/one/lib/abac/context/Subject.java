package vn.truongngo.apartcom.one.lib.abac.context;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Represents a Subject with userId, roles, and attributes for ABAC.
 * @author Truong Ngo
 */
@Data
public class Subject {

    private String userId;
    private List<String> roles;
    private Map<String, Object> attributes;

    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
