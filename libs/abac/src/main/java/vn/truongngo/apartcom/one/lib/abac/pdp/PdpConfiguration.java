package vn.truongngo.apartcom.one.lib.abac.pdp;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for the Policy Decision Point engine.
 * @author Truong Ngo
 */
@Data
public class PdpConfiguration {

    private DecisionStrategy decisionStrategy;
    private Map<String, Object> config;

    public PdpConfiguration(DecisionStrategy decisionStrategy) {
        if (decisionStrategy == null) {
            throw new IllegalArgumentException("Decision strategy cannot be null.");
        }
        this.decisionStrategy = decisionStrategy;
        this.config = new LinkedHashMap<>();
    }
}
