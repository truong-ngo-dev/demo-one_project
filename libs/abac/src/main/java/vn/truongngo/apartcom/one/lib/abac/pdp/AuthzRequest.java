package vn.truongngo.apartcom.one.lib.abac.pdp;

import lombok.AllArgsConstructor;
import lombok.Data;
import vn.truongngo.apartcom.one.lib.abac.context.*;
import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;

/**
 * Represents an authorization request in the ABAC system.
 * @author Truong Ngo
 */
@Data
@AllArgsConstructor
public class AuthzRequest {

    private Subject subject;
    private Resource object;
    private Action action;
    private Environment environment;
    private AbstractPolicy policy;

    public EvaluationContext getEvaluationContext() {
        return new EvaluationContext(subject, object, action, environment);
    }
}
