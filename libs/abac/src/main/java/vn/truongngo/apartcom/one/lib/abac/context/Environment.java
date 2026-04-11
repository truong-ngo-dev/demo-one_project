package vn.truongngo.apartcom.one.lib.abac.context;

import lombok.Data;

import java.util.Map;

/**
 * Manages environment-specific data for ABAC evaluation.
 * @author Truong Ngo
 */
@Data
public class Environment {

    private Map<String, Object> global;
    private Map<String, Object> service;

    public void addGlobalEnv(String key, Object value) {
        global.put(key, value);
    }

    public void addServiceEnv(String key, Object value) {
        service.put(key, value);
    }

    public Object getGlobalEnv(String key) {
        return global.get(key);
    }

    public Object getServiceEnv(String key) {
        return service.get(key);
    }
}
