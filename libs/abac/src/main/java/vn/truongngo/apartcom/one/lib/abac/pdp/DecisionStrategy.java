package vn.truongngo.apartcom.one.lib.abac.pdp;

import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationResult;

/**
 * Enum representing strategies to decide authorization outcome based on an EvaluationResult.
 * @author Truong Ngo
 */
public enum DecisionStrategy {

    DEFAULT_DENY {
        @Override
        public AuthzDecision.Decision decide(EvaluationResult result) {
            return (result.isPermit() || result.isDeny()) ? permitDenyDecide(result) : AuthzDecision.Decision.DENY;
        }
    },

    DEFAULT_PERMIT {
        @Override
        public AuthzDecision.Decision decide(EvaluationResult result) {
            return (result.isPermit() || result.isDeny()) ? permitDenyDecide(result) : AuthzDecision.Decision.PERMIT;
        }
    },

    NOT_APPLICABLE_PERMIT_INDETERMINATE_DENY {
        @Override
        public AuthzDecision.Decision decide(EvaluationResult result) {
            if (result.isPermit() || result.isDeny()) return permitDenyDecide(result);
            if (result.isIndeterminate()) return AuthzDecision.Decision.DENY;
            return AuthzDecision.Decision.PERMIT;
        }
    };

    public abstract AuthzDecision.Decision decide(EvaluationResult result);

    public AuthzDecision.Decision permitDenyDecide(EvaluationResult result) {
        return result.isPermit() ? AuthzDecision.Decision.PERMIT : AuthzDecision.Decision.DENY;
    }
}
