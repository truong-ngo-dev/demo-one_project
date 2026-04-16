package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpression;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.AbacExpressionJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.NamedExpressionJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.NamedExpressionJpaRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NamedExpressionPersistenceAdapter implements NamedExpressionRepository {

    private final NamedExpressionJpaRepository namedExpressionJpaRepository;
    private final AbacExpressionJpaRepository abacExpressionJpaRepository;

    @Override
    public Optional<NamedExpression> findById(NamedExpressionId id) {
        return namedExpressionJpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<NamedExpression> findBySpel(String spel) {
        return namedExpressionJpaRepository.findBySpel(spel).map(this::toDomain);
    }

    @Override
    public List<NamedExpression> findAll() {
        return namedExpressionJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public NamedExpression save(NamedExpression expr) {
        NamedExpressionJpaEntity entity = expr.getId() != null
                ? namedExpressionJpaRepository.findById(expr.getId().value())
                        .orElse(new NamedExpressionJpaEntity())
                : new NamedExpressionJpaEntity();
        entity.setName(expr.getName());
        entity.setSpel(expr.getSpel());
        return toDomain(namedExpressionJpaRepository.save(entity));
    }

    @Override
    public void delete(NamedExpressionId id) {
        namedExpressionJpaRepository.deleteById(id.value());
    }

    @Override
    public boolean isInUse(NamedExpressionId id) {
        return !abacExpressionJpaRepository.findAllByNamedExpressionId(id.value()).isEmpty();
    }

    private NamedExpression toDomain(NamedExpressionJpaEntity entity) {
        return NamedExpression.reconstitute(
                NamedExpressionId.of(entity.getId()),
                entity.getName(),
                entity.getSpel());
    }
}
