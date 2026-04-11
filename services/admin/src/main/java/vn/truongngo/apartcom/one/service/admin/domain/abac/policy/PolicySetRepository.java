package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PolicySetRepository {

    PolicySetDefinition save(PolicySetDefinition policySet);

    Optional<PolicySetDefinition> findById(PolicySetId id);

    Page<PolicySetDefinition> findAll(String keyword, Pageable pageable);

    void delete(PolicySetId id);

    boolean existsByName(String name);

    boolean existsPoliciesFor(PolicySetId id);

    List<PolicySetDefinition> findAllRoot();
}
