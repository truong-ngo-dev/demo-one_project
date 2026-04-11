package vn.truongngo.apartcom.one.lib.abac.algorithm;

import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;
import vn.truongngo.apartcom.one.lib.abac.domain.Principle;
import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationResult;
import vn.truongngo.apartcom.one.lib.abac.evaluation.ExpressionResult;
import vn.truongngo.apartcom.one.lib.abac.evaluation.IndeterminateCause;
import vn.truongngo.apartcom.one.lib.abac.evaluation.PolicyEvaluators;

import java.util.List;

/**
 * Implements the "Only One Applicable" combining algorithm.
 * @author Truong Ngo
 */
public class OnlyOneApplicableCombineAlgorithm<E extends Principle> implements CombineAlgorithm<E> {

    @Override
    public EvaluationResult evaluate(List<E> principles, EvaluationContext context) {
        boolean atLeastOne = false;
        E selectedPolicy = null;
        for (E policy : principles) {
            ExpressionResult applicableResult = policy.isApplicable(context);
            if (applicableResult.isIndeterminate()) {
                return buildIndeterminateResult(policy, List.of(applicableResult.getIndeterminateCause()));
            }
            if (applicableResult.isMatch()) {
                if (atLeastOne) {
                    return buildIndeterminateResult(policy, List.of(applicableResult.getIndeterminateCause()));
                } else {
                    atLeastOne = true;
                    selectedPolicy = policy;
                }
            }
        }
        if (atLeastOne) {
            return PolicyEvaluators.evaluate(context, selectedPolicy);
        } else {
            return new EvaluationResult(EvaluationResult.EvaluationResultType.NOT_APPLICABLE);
        }
    }

    private EvaluationResult buildIndeterminateResult(Principle principle, List<IndeterminateCause> subCauses) {
        IndeterminateCause cause = new IndeterminateCause(IndeterminateCause.IndeterminateCauseType.PROCESSING_ERROR, subCauses);
        String element = principle instanceof AbstractPolicy ? "Policy" : "PolicySet";
        cause.buildDefaultDescription(element, principle.getId());
        return new EvaluationResult(EvaluationResult.EvaluationResultType.INDETERMINATE, cause);
    }
}
