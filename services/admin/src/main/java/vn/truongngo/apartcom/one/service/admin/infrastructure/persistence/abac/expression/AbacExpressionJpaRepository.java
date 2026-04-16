package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AbacExpressionJpaRepository extends JpaRepository<AbacExpressionJpaEntity, Long> {

    List<AbacExpressionJpaEntity> findAllByParentId(Long parentId);

    List<AbacExpressionJpaEntity> findAllByNamedExpressionId(Long namedExpressionId);
}
