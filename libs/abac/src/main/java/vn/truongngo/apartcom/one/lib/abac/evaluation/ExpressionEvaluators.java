package vn.truongngo.apartcom.one.lib.abac.evaluation;

import vn.truongngo.apartcom.one.lib.abac.context.EvaluationContext;
import vn.truongngo.apartcom.one.lib.abac.domain.Expression;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for evaluating logical expressions using SpEL.
 * @author Truong Ngo
 */
public class ExpressionEvaluators {

    public static ExpressionResult evaluate(EvaluationContext context, Expression expression) {
        if (expression.isLiteral()) {
            return literalEvaluator.evaluate(context, expression);
        } else {
            return switch (expression.getCombinationType()) {
                case AND -> conjunctionEvaluator.evaluate(context, expression.getSubExpressions());
                case OR -> disConjunctionEvaluator.evaluate(context, expression.getSubExpressions());
            };
        }
    }

    @FunctionalInterface
    public interface ExpressionEvaluator {
        ExpressionResult evaluate(EvaluationContext context, Expression expression);
    }

    public static ExpressionEvaluator literalEvaluator = (context, expression) -> {
        String expr = expression.getExpression();
        if (Objects.isNull(expr)) {
            IndeterminateCause cause = new IndeterminateCause();
            cause.setCode(IndeterminateCause.IndeterminateCauseType.SYNTAX_ERROR);
            cause.setDescription("Expression is null");
            return new ExpressionResult(ExpressionResult.ResultType.INDETERMINATE, cause);
        }
        ExpressionResult result = new ExpressionResult();
        IndeterminateCause cause = null;
        try {
            ExpressionParser parser = new SpelExpressionParser();
            org.springframework.expression.Expression exp = parser.parseExpression(expr);
            Boolean expValue = exp.getValue(context, Boolean.class);
            ExpressionResult.ResultType resultType = Boolean.TRUE.equals(expValue) ?
                    ExpressionResult.ResultType.MATCH :
                    ExpressionResult.ResultType.NO_MATCH;
            result.setResultType(resultType);
        } catch (ParseException | EvaluationException | IllegalAccessError exception) {
            result.setResultType(ExpressionResult.ResultType.INDETERMINATE);
            cause = new IndeterminateCause(IndeterminateCause.IndeterminateCauseType.SYNTAX_ERROR);
        }
        if (Objects.nonNull(cause)) {
            result.setIndeterminateCause(cause);
        }
        return result;
    };

    @FunctionalInterface
    public interface CompositeExpressionEvaluator {
        default ExpressionResult evaluate(EvaluationContext context, List<Expression> expressions) {
            if (Objects.isNull(expressions) || expressions.isEmpty()) {
                IndeterminateCause cause = new IndeterminateCause();
                cause.setCode(IndeterminateCause.IndeterminateCauseType.SYNTAX_ERROR);
                cause.setDescription("Sub expression is empty");
                return new ExpressionResult(ExpressionResult.ResultType.INDETERMINATE, cause);
            } else {
                return evaluateInternal(context, expressions);
            }
        }
        ExpressionResult evaluateInternal(EvaluationContext context, List<Expression> expressions);
    }

    public static CompositeExpressionEvaluator conjunctionEvaluator = (context, expressions) -> {
        boolean atLeastOneIndeterminate = false;
        List<ExpressionResult> expressionResults = expressions.stream().map(r -> ExpressionEvaluators.evaluate(context, r)).toList();
        List<IndeterminateCause> indeterminateCauses = expressionResults.stream()
                .filter(ExpressionResult::isIndeterminate)
                .map(ExpressionResult::getIndeterminateCause)
                .toList();

        for (ExpressionResult r : expressionResults) {
            if (r.isNotMatch()) {
                return new ExpressionResult(ExpressionResult.ResultType.NO_MATCH);
            }
            if (r.isIndeterminate()) {
                atLeastOneIndeterminate = true;
            }
        }

        if (atLeastOneIndeterminate) {
            IndeterminateCause cause = new IndeterminateCause(IndeterminateCause.IndeterminateCauseType.PROCESSING_ERROR, indeterminateCauses);
            return new ExpressionResult(ExpressionResult.ResultType.INDETERMINATE, cause);
        } else {
            return new ExpressionResult(ExpressionResult.ResultType.MATCH);
        }
    };

    public static CompositeExpressionEvaluator disConjunctionEvaluator = (context, expressions) -> {
        boolean atLeastOneIndeterminate = false;
        List<ExpressionResult> expressionResults = expressions.stream().map(r -> ExpressionEvaluators.evaluate(context, r)).toList();
        List<IndeterminateCause> indeterminateCauses = expressionResults.stream()
                .filter(ExpressionResult::isIndeterminate)
                .map(ExpressionResult::getIndeterminateCause)
                .toList();

        for (ExpressionResult r : expressionResults) {
            if (r.isMatch()) {
                return new ExpressionResult(ExpressionResult.ResultType.MATCH);
            }
            if (r.isIndeterminate()) {
                atLeastOneIndeterminate = true;
            }
        }

        if (atLeastOneIndeterminate) {
            IndeterminateCause cause = new IndeterminateCause(IndeterminateCause.IndeterminateCauseType.PROCESSING_ERROR, indeterminateCauses);
            return new ExpressionResult(ExpressionResult.ResultType.INDETERMINATE, cause);
        } else {
            return new ExpressionResult(ExpressionResult.ResultType.NO_MATCH);
        }
    };
}
