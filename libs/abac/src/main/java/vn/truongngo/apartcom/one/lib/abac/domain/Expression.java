package vn.truongngo.apartcom.one.lib.abac.domain;

import lombok.Data;

import java.util.List;

/**
 * Represents an XACML Expression.
 * @author Truong Ngo
 */
@Data
public class Expression {

    private String id;
    private String description;
    private Type type;
    private String expression;
    private List<Expression> subExpressions;
    private CombinationType combinationType;

    public boolean isLiteral() {
        return type == Type.LITERAL;
    }

    public enum CombinationType {
        AND,
        OR
    }

    public enum Type {
        LITERAL,
        COMPOSITION
    }
}
