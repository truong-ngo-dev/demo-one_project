package vn.truongngo.apartcom.one.lib.abac.evaluation;

import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithm;
import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmFactory;
import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.domain.*;

import java.util.Objects;

/**
 * Provides evaluation logic for policy elements in the ABAC system.
 * @author Truong Ngo
 */
public class PolicyEvaluators {

    public static <E extends Principle> EvaluationResult evaluate(EvaluationContext context, E policy) {
        return (policy instanceof Rule rule) ?
                ruleEvaluator.evaluate(context, rule) :
                policyEvaluator.evaluate(context, (AbstractPolicy) policy);
    }

    public interface PolicyElementEvaluator<E extends Principle> {
        EvaluationResult evaluate(EvaluationContext context, E policy);
    }

    public static PolicyElementEvaluator<Rule> ruleEvaluator = (context, rule) -> {
        ExpressionResult target = rule.getTarget() == null ?
                new ExpressionResult(ExpressionResult.ResultType.MATCH) :
                ExpressionEvaluators.evaluate(context, rule.getTarget());
        ExpressionResult condition = rule.getCondition() == null ?
                new ExpressionResult(ExpressionResult.ResultType.MATCH) :
                ExpressionEvaluators.evaluate(context, rule.getCondition());

        EvaluationResult result;
        if (target.isMatch()) {
            if (condition.isMatch()) {
                result = rule.getEffect().equals(Rule.Effect.PERMIT) ?
                        new EvaluationResult(EvaluationResult.EvaluationResultType.PERMIT) :
                        new EvaluationResult(EvaluationResult.EvaluationResultType.DENY);
            } else if (condition.isNotMatch()) {
                result = new EvaluationResult(EvaluationResult.EvaluationResultType.NOT_APPLICABLE);
            } else {
                result = CombineAlgorithm.evaluateRuleIfIndeterminate(rule, condition, "Condition");
            }
        } else if (target.isNotMatch()) {
            result = new EvaluationResult(EvaluationResult.EvaluationResultType.NOT_APPLICABLE);
        } else {
            result = CombineAlgorithm.evaluateRuleIfIndeterminate(rule, target, "Target");
        }

        if (context.isTracingEnabled()) {
            boolean targetMatched = target.isMatch();
            Boolean conditionMatched = targetMatched ? condition.isMatch() : null;
            boolean wasDeciding = result.isPermit() || result.isDeny();
            context.addTraceEntry(new RuleTraceEntry(
                    rule.getId(),
                    rule.getDescription(),
                    rule.getEffect(),
                    targetMatched,
                    conditionMatched,
                    wasDeciding
            ));
        }

        return result;
    };

    public static PolicyElementEvaluator<AbstractPolicy> policyEvaluator = (context, policy) -> {
        ExpressionResult target = ExpressionEvaluators.evaluate(context, policy.getTarget());
        if (target.isNotMatch()) {
            return new EvaluationResult(EvaluationResult.EvaluationResultType.NOT_APPLICABLE);
        }

        EvaluationResult combineResult;
        String element;
        if (policy instanceof PolicySet) {
            element = "PolicySet";
            CombineAlgorithm<AbstractPolicy> combineAlgorithm = CombineAlgorithmFactory.from(policy.getCombineAlgorithmName(), AbstractPolicy.class);
            combineResult = combineAlgorithm.evaluate(((PolicySet) policy).getPolicies(), context);
        } else {
            element = "Policy";
            inheritTargetIfRuleOmitted((Policy) policy);
            CombineAlgorithm<Rule> combineAlgorithm = CombineAlgorithmFactory.from(policy.getCombineAlgorithmName(), Rule.class);
            combineResult = combineAlgorithm.evaluate(((Policy) policy).getRules(), context);
        }

        if (target.isMatch()) {
            if (combineResult.isIndeterminate()) {
                combineResult.getIndeterminateCause().buildDefaultDescription(element, policy.getId());
            }
            return combineResult;
        }
        return CombineAlgorithm.evaluatePolicyIfIndeterminateTarget(combineResult, policy, target);
    };

    public static void inheritTargetIfRuleOmitted(Policy policy) {
        policy.getRules().forEach(r -> {
            if (Objects.isNull(r.getTarget())) {
                r.setTarget(policy.getTarget());
            }
        });
    }
}
