package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy_set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolicySetJpaRepository extends JpaRepository<PolicySetJpaEntity, Long> {

    boolean existsByName(String name);

    @Query("SELECT ps FROM PolicySetJpaEntity ps WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(ps.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PolicySetJpaEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(p) > 0 FROM PolicyJpaEntity p WHERE p.policySetId = :policySetId")
    boolean existsPoliciesByPolicySetId(@Param("policySetId") Long policySetId);

    List<PolicySetJpaEntity> findAllByIsRootTrue();
}
