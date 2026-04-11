package vn.truongngo.apartcom.one.lib.abac.context;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a resource in the ABAC system.
 * @author Truong Ngo
 */
@Data
public class Resource {

    private String name;
    private List<String> subResourceName;
    private Object data;
    private Map<String, Object> attributes;

    public Resource() {
        this.attributes = new LinkedHashMap<>();
    }

    public Resource(String name, Object data) {
        this.name = name;
        this.data = data;
        this.attributes = new LinkedHashMap<>();
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }
}
