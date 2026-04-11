package vn.truongngo.apartcom.one.lib.abac.rap;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for resource access in the ABAC system.
 * @author Truong Ngo
 */
@Getter
public class ResourceAccessConfig {

    private final String resourceNameExtractor;
    private final Set<String> ignoredPaths;
    private final Map<String, Object> config;

    public ResourceAccessConfig(String resourceNameExtractor, Set<String> ignoredPaths) {
        this.resourceNameExtractor = resourceNameExtractor;
        this.ignoredPaths = ignoredPaths;
        this.config = new LinkedHashMap<>();
    }
}
