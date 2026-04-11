package vn.truongngo.apartcom.one.lib.abac.algorithm;

import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.domain.Principle;
import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationResult;
import vn.truongngo.apartcom.one.lib.abac.evaluation.IndeterminateCause;

import java.util.List;

/**
 * Implements the "Permit Overrides" combining algorithm.
 * @author Truong Ngo
 */
public class PermitOverridesCombineAlgorithm<E extends Principle> implements CombineAlgorithm<E> {

    @Override
    public EvaluationResult evaluate(List<E> principles, EvaluationContext context) {
        List<EvaluationResult> results = getListEvaluationResult(principles, context);

        boolean atLeastOneInD = false, atLeastOneInP = false, atLeastOneInDP = false, atLeastOneDeny = false;

        for (EvaluationResult result : results) {
            if (result.isDeny()) atLeastOneDeny = true;
            if (result.isPermit()) return new EvaluationResult(EvaluationResult.EvaluationResultType.PERMIT);
            if (result.getResult().equals(EvaluationResult.EvaluationResultType.INDETERMINATE_D)) atLeastOneInD = true;
            if (result.getResult().equals(EvaluationResult.EvaluationResultType.INDETERMINATE_P)) atLeastOneInP = true;
            if (result.getResult().equals(EvaluationResult.EvaluationResultType.INDETERMINATE_DP)) atLeastOneInDP = true;
        }

        IndeterminateCause cause = getIndeterminateEvaluationCause(results);

        if (atLeastOneInDP) return new EvaluationResult(EvaluationResult.EvaluationResultType.INDETERMINATE_DP, cause);
        if (atLeastOneInP && (atLeastOneInD || atLeastOneDeny)) return new EvaluationResult(EvaluationResult.EvaluationResultType.INDETERMINATE_DP, cause);
        if (atLeastOneInP) return new EvaluationResult(EvaluationResult.EvaluationResultType.INDETERMINATE_P, cause);
        if (atLeastOneDeny) return new EvaluationResult(EvaluationResult.EvaluationResultType.DENY);
        if (atLeastOneInD) return new EvaluationResult(EvaluationResult.EvaluationResultType.INDETERMINATE_D, cause);
        return new EvaluationResult(EvaluationResult.EvaluationResultType.NOT_APPLICABLE);
    }
}
