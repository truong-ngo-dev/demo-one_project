package vn.truongngo.apartcom.one.service.admin.domain.abac.expression;

import java.util.List;
import java.util.Optional;

public interface NamedExpressionRepository {

    Optional<NamedExpression> findById(NamedExpressionId id);

    Optional<NamedExpression> findBySpel(String spel);

    List<NamedExpression> findAll();

    NamedExpression save(NamedExpression expr);

    void delete(NamedExpressionId id);

    /** Returns true if any abac_expression row references this named expression. */
    boolean isInUse(NamedExpressionId id);
}
