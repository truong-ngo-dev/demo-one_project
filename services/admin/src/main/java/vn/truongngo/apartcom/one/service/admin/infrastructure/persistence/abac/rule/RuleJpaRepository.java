package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.rule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleJpaRepository extends JpaRepository<RuleJpaEntity, Long> {

    List<RuleJpaEntity> findByPolicyIdOrderByOrderIndex(Long policyId);
}
