package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository {

    PolicyDefinition save(PolicyDefinition policy);

    Optional<PolicyDefinition> findById(PolicyId id);

    List<PolicyDefinition> findByPolicySetId(PolicySetId policySetId);

    void delete(PolicyId id);
}
