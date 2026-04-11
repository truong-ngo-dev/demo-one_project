package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

/**
 * Value object representing a LITERAL SpEL expression.
 * Phase 1 supports LITERAL type only; id is DB-assigned on first persist.
 */
public record ExpressionVO(Long id, String spElExpression) {

    public static ExpressionVO create(String spElExpression) {
        return new ExpressionVO(null, spElExpression);
    }

    public static ExpressionVO reconstitute(Long id, String spElExpression) {
        return new ExpressionVO(id, spElExpression);
    }
}
