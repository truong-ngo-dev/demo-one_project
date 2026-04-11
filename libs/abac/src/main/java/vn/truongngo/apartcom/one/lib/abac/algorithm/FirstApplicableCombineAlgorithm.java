package vn.truongngo.apartcom.one.lib.abac.algorithm;

import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.domain.Principle;
import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationResult;
import vn.truongngo.apartcom.one.lib.abac.evaluation.IndeterminateCause;

import java.util.List;

/**
 * Implements the "First Applicable" combining algorithm.
 * @author Truong Ngo
 */
public class FirstApplicableCombineAlgorithm<E extends Principle> implements CombineAlgorithm<E> {

    @Override
    public EvaluationResult evaluate(List<E> principles, EvaluationContext context) {
        List<EvaluationResult> results = getListEvaluationResult(principles, context);

        List<IndeterminateCause> indeterminate = results.stream()
                .filter(EvaluationResult::isIndeterminate)
                .map(EvaluationResult::getIndeterminateCause)
                .toList();
        IndeterminateCause cause = new IndeterminateCause(IndeterminateCause.IndeterminateCauseType.PROCESSING_ERROR, indeterminate);

        for (EvaluationResult result : results) {
            if (result.isPermit() || result.isDeny() || result.isIndeterminate()) {
                return result.isIndeterminate() ?
                        new EvaluationResult(EvaluationResult.EvaluationResultType.INDETERMINATE, cause) :
                        new EvaluationResult(result.getResult());
            }
        }
        return new EvaluationResult(EvaluationResult.EvaluationResultType.NOT_APPLICABLE);
    }
}
