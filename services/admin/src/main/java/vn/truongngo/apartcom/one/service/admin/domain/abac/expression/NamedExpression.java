package vn.truongngo.apartcom.one.service.admin.domain.abac.expression;

import lombok.Getter;

/**
 * Aggregate Root for a reusable named SpEL expression.
 * Has its own lifecycle — not cascaded by Policy or Rule.
 */
@Getter
public class NamedExpression {

    private final NamedExpressionId id;
    private final String name;
    private final String spel;

    private NamedExpression(NamedExpressionId id, String name, String spel) {
        this.id   = id;
        this.name = name;
        this.spel = spel;
    }

    public static NamedExpression create(String name, String spel) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (spel == null || spel.isBlank()) throw new IllegalArgumentException("spel is required");
        return new NamedExpression(null, name, spel);
    }

    public static NamedExpression reconstitute(NamedExpressionId id, String name, String spel) {
        return new NamedExpression(id, name, spel);
    }

    public NamedExpression rename(String newName) {
        if (newName == null || newName.isBlank()) throw new IllegalArgumentException("name is required");
        return new NamedExpression(this.id, newName, this.spel);
    }

    public NamedExpression updateSpel(String newSpel) {
        if (newSpel == null || newSpel.isBlank()) throw new IllegalArgumentException("spel is required");
        return new NamedExpression(this.id, this.name, newSpel);
    }
}
