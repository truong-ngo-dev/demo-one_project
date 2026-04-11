package vn.truongngo.apartcom.one.lib.abac.algorithm;

import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.domain.*;
import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationResult;
import vn.truongngo.apartcom.one.lib.abac.evaluation.ExpressionResult;
import vn.truongngo.apartcom.one.lib.abac.evaluation.IndeterminateCause;
import vn.truongngo.apartcom.one.lib.abac.evaluation.PolicyEvaluators;

import java.util.List;

/**
 * Defines a combination algorithm for Principle elements.
 * @author Truong Ngo
 */
public interface CombineAlgorithm<E extends Principle> {

    EvaluationResult evaluate(List<E> principles, EvaluationContext context);

    default List<EvaluationResult> getListEvaluationResult(List<E> principles, EvaluationContext context) {
        return principles.stream().map(p -> PolicyEvaluators.evaluate(context, p)).toList();
    }

    static EvaluationResult evaluatePolicyIfIndeterminateTarget(EvaluationResult combineResult, Principle principle, ExpressionResult target) {
        EvaluationResult.EvaluationResultType resultType = switch (combineResult.getResult()) {
            case NOT_APPLICABLE -> EvaluationResult.EvaluationResultType.NOT_APPLICABLE;
            case PERMIT, INDETERMINATE_P -> EvaluationResult.EvaluationResultType.INDETERMINATE_P;
            case DENY, INDETERMINATE_D -> EvaluationResult.EvaluationResultType.INDETERMINATE_D;
            case INDETERMINATE_DP, INDETERMINATE -> EvaluationResult.EvaluationResultType.INDETERMINATE_DP;
        };

        target.getIndeterminateCause().buildDefaultDescription("Target", principle.getTarget().getId());
        IndeterminateCause cause = new IndeterminateCause(IndeterminateCause.IndeterminateCauseType.PROCESSING_ERROR, List.of(target.getIndeterminateCause()));

        return new EvaluationResult(resultType, cause);
    }

    static EvaluationResult evaluateRuleIfIndeterminate(Rule rule, ExpressionResult expressionResult, String element) {
        String id = element.equals("Target") ? rule.getTarget().getId() : rule.getCondition().getId();
        expressionResult.getIndeterminateCause().buildDefaultDescription(element, id);
        IndeterminateCause cause = new IndeterminateCause(IndeterminateCause.IndeterminateCauseType.PROCESSING_ERROR);
        cause.buildDefaultDescription("Rule", rule.getId());
        cause.setSubIndeterminateCauses(List.of(expressionResult.getIndeterminateCause()));
        EvaluationResult.EvaluationResultType resultType = rule.getEffect().equals(Rule.Effect.PERMIT) ?
                EvaluationResult.EvaluationResultType.INDETERMINATE_P :
                EvaluationResult.EvaluationResultType.INDETERMINATE_D;
        return new EvaluationResult(resultType, cause);
    }

    default IndeterminateCause getIndeterminateEvaluationCause(List<EvaluationResult> results) {
        List<IndeterminateCause> indeterminate = results.stream()
                .filter(EvaluationResult::isIndeterminate)
                .map(EvaluationResult::getIndeterminateCause)
                .toList();
        return new IndeterminateCause(IndeterminateCause.IndeterminateCauseType.PROCESSING_ERROR, indeterminate);
    }
}
