package vn.truongngo.apartcom.one.service.admin.domain.abac.expression;

public record NamedExpressionId(Long value) {

    public static NamedExpressionId of(Long value) {
        if (value == null) throw new IllegalArgumentException("NamedExpressionId value must not be null");
        return new NamedExpressionId(value);
    }
}
