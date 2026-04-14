package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Validates SpEL expression syntax without evaluating against a real context.
 */
public class SpelValidator {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    private SpelValidator() {}

    /**
     * Returns true if the expression is syntactically valid SpEL.
     */
    public static boolean isValid(String expression) {
        if (expression == null || expression.isBlank()) return false;
        try {
            PARSER.parseExpression(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Throws PolicyException if the expression is not valid SpEL.
     */
    public static void validate(String expression) {
        if (!isValid(expression)) {
            throw PolicyException.invalidSpElExpression();
        }
    }
}
