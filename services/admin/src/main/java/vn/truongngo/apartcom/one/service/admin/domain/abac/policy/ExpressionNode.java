package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionId;

import java.util.List;

/**
 * Value Object representing an expression tree node.
 * Pure domain concept — carries no DB identity.
 *
 * Three variants:
 *  - Inline      : raw SpEL string, optionally named for display
 *  - LibraryRef  : cross-aggregate reference to a NamedExpression by ID
 *  - Composition : AND / OR block containing child nodes
 */
public sealed interface ExpressionNode
        permits ExpressionNode.Inline, ExpressionNode.LibraryRef, ExpressionNode.Composition {

    /** Operators for Composition nodes. */
    enum Operator { AND, OR }

    /**
     * Leaf node containing a SpEL string directly.
     * TRANSIENT ONLY — used in request/response mapping, never persisted directly to DB.
     * Application layer always promotes Inline → NamedExpression + LibraryRef before saving.
     * {@code name} is required when creating a new expression (used as NamedExpression name).
     */
    record Inline(String name, String spel) implements ExpressionNode {
        public boolean isNamed() { return name != null && !name.isBlank(); }
    }

    /** Leaf node that references a NamedExpression by its aggregate-root ID. */
    record LibraryRef(NamedExpressionId refId) implements ExpressionNode {}

    /** Structural AND / OR block. */
    record Composition(Operator operator, List<ExpressionNode> children) implements ExpressionNode {}
}
