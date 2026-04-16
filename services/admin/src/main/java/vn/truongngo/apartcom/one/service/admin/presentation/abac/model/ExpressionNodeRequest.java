package vn.truongngo.apartcom.one.service.admin.presentation.abac.model;

import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionNode;

import java.util.List;

/**
 * Recursive request model for expression trees.
 * type: "INLINE" | "LIBRARY_REF" | "COMPOSITION"
 */
public record ExpressionNodeRequest(
        String type,
        String name,        // INLINE only — optional name for display
        String spel,        // INLINE only
        Long refId,         // LIBRARY_REF only
        String operator,    // COMPOSITION only: "AND" | "OR"
        List<ExpressionNodeRequest> children  // COMPOSITION only
) {
    public ExpressionNode toDomain() {
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "INLINE" -> {
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("INLINE expression requires a non-blank name");
                }
                if (spel == null || spel.isBlank()) {
                    throw new IllegalArgumentException("INLINE expression requires a non-blank spel");
                }
                yield new ExpressionNode.Inline(name, spel);
            }
            case "LIBRARY_REF" -> new ExpressionNode.LibraryRef(NamedExpressionId.of(refId));
            case "COMPOSITION" -> {
                ExpressionNode.Operator op = "OR".equalsIgnoreCase(operator)
                        ? ExpressionNode.Operator.OR : ExpressionNode.Operator.AND;
                List<ExpressionNode> childNodes = children == null ? List.of()
                        : children.stream().map(ExpressionNodeRequest::toDomain).toList();
                yield new ExpressionNode.Composition(op, childNodes);
            }
            default -> throw new IllegalArgumentException("Unknown expression node type: " + type);
        };
    }
}
