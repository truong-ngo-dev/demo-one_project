package vn.truongngo.apartcom.one.lib.abac.algorithm;

import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.domain.Principle;
import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationResult;

import java.util.List;

/**
 * Implements the "Deny Unless Permit" combining algorithm.
 * @author Truong Ngo
 */
public class DenyUnlessPermitCombineAlgorithm<E extends Principle> implements CombineAlgorithm<E> {

    @Override
    public EvaluationResult evaluate(List<E> principles, EvaluationContext context) {
        List<EvaluationResult> results = getListEvaluationResult(principles, context);
        return results.stream().anyMatch(EvaluationResult::isPermit) ?
                new EvaluationResult(EvaluationResult.EvaluationResultType.PERMIT) :
                new EvaluationResult(EvaluationResult.EvaluationResultType.DENY);
    }
}
