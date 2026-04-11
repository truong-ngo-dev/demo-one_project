package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyJpaRepository extends JpaRepository<PolicyJpaEntity, Long> {

    List<PolicyJpaEntity> findByPolicySetId(Long policySetId);
}
