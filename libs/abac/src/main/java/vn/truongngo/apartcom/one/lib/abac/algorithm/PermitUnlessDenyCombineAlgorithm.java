package vn.truongngo.apartcom.one.lib.abac.algorithm;

import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.domain.Principle;
import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationResult;

import java.util.List;

/**
 * Implements the "Permit Unless Deny" combining algorithm.
 * @author Truong Ngo
 */
public class PermitUnlessDenyCombineAlgorithm<E extends Principle> implements CombineAlgorithm<E> {

    @Override
    public EvaluationResult evaluate(List<E> principles, EvaluationContext context) {
        List<EvaluationResult> results = getListEvaluationResult(principles, context);
        return results.stream().anyMatch(EvaluationResult::isDeny) ?
                new EvaluationResult(EvaluationResult.EvaluationResultType.DENY) :
                new EvaluationResult(EvaluationResult.EvaluationResultType.PERMIT);
    }
}
