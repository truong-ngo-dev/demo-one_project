package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NamedExpressionJpaRepository extends JpaRepository<NamedExpressionJpaEntity, Long> {

    Optional<NamedExpressionJpaEntity> findBySpel(String spel);
}
