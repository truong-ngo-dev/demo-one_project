package vn.truongngo.apartcom.one.lib.abac.domain;

import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.evaluation.ExpressionEvaluators;
import vn.truongngo.apartcom.one.lib.abac.evaluation.ExpressionResult;

/**
 * Marker interface for policy elements such as Rule, Policy, and PolicySet.
 * @author Truong Ngo
 */
public interface Principle {

    String getId();

    String getDescription();

    Expression getTarget();

    default ExpressionResult isApplicable(EvaluationContext context) {
        Expression target = getTarget();
        if (target == null) {
            return new ExpressionResult(ExpressionResult.ResultType.MATCH);
        }
        ExpressionResult targetApplicable = ExpressionEvaluators.evaluate(context, target);
        if (!targetApplicable.isIndeterminate()) {
            return targetApplicable;
        }
        targetApplicable.getIndeterminateCause().buildDefaultDescription("Target", target.getId());
        return targetApplicable;
    }
}
