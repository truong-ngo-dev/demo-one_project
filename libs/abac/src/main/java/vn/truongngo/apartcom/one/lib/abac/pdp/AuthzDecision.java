package vn.truongngo.apartcom.one.lib.abac.pdp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents an authorization decision made by the PDP.
 * @author Truong Ngo
 */
@Data
@AllArgsConstructor
public class AuthzDecision {

    private Decision decision;
    private Long timestamp;
    private Object details;

    public enum Decision {
        PERMIT,
        DENY
    }

    public AuthzDecision(Decision decision, Object details) {
        this.decision = decision;
        this.timestamp = System.currentTimeMillis();
        this.details = details;
    }

    public boolean isPermit() {
        return decision == Decision.PERMIT;
    }

    public boolean isDeny() {
        return decision == Decision.DENY;
    }
}
