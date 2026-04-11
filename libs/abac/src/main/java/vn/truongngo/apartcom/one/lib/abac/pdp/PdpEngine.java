package vn.truongngo.apartcom.one.lib.abac.pdp;

import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;
import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationDetails;
import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationResult;
import vn.truongngo.apartcom.one.lib.abac.evaluation.PolicyEvaluators;

/**
 * Policy Decision Point (PDP) engine that evaluates authorization requests.
 * @author Truong Ngo
 */
public class PdpEngine {

    private final PdpConfiguration configuration;

    public PdpEngine(PdpConfiguration configuration) {
        this.configuration = configuration;
    }

    public EvaluationResult evaluate(AuthzRequest authzRequest) {
        AbstractPolicy policy = authzRequest.getPolicy();
        EvaluationContext context = authzRequest.getEvaluationContext();
        return PolicyEvaluators.evaluate(context, policy);
    }

    public AuthzDecision authorize(AuthzRequest authzRequest) {
        EvaluationResult evaluationResult = evaluate(authzRequest);
        AuthzDecision.Decision decision = configuration.getDecisionStrategy().decide(evaluationResult);
        Object details = evaluationResult.isIndeterminate() ?
                evaluationResult.getIndeterminateCause() :
                evaluationResult.isNotApplicable() ? "No policy applicable" : null;
        return new AuthzDecision(decision, details);
    }

    /**
     * Like {@link #authorize(AuthzRequest)} but also collects a per-rule evaluation trace.
     * The returned {@link AuthzDecision#getDetails()} will be an {@link EvaluationDetails}
     * containing the cause (if any) and the ordered list of {@code RuleTraceEntry}.
     */
    public AuthzDecision authorizeWithTrace(AuthzRequest authzRequest) {
        EvaluationContext context = authzRequest.getEvaluationContext();
        context.enableTracing();
        EvaluationResult evaluationResult = PolicyEvaluators.evaluate(context, authzRequest.getPolicy());
        AuthzDecision.Decision decision = configuration.getDecisionStrategy().decide(evaluationResult);
        Object cause = evaluationResult.isIndeterminate() ?
                evaluationResult.getIndeterminateCause() :
                evaluationResult.isNotApplicable() ? "No policy applicable" : null;
        return new AuthzDecision(decision, new EvaluationDetails(cause, context.getTraceEntries()));
    }
}
