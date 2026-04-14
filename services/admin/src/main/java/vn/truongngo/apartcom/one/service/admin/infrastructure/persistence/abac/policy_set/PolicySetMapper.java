package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy_set;

import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;

public class PolicySetMapper {

    public static PolicySetDefinition toDomain(PolicySetJpaEntity entity) {
        return PolicySetDefinition.reconstitute(
                PolicySetId.of(entity.getId()),
                entity.getName(),
                Scope.valueOf(entity.getScope()),
                CombineAlgorithmName.valueOf(entity.getCombineAlgorithm()),
                entity.isRoot(),
                entity.getTenantId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static PolicySetJpaEntity toEntity(PolicySetDefinition domain) {
        PolicySetJpaEntity entity = new PolicySetJpaEntity();
        entity.setId(domain.getId() != null ? domain.getId().getValue() : null);
        entity.setName(domain.getName());
        entity.setScope(domain.getScope().name());
        entity.setCombineAlgorithm(domain.getCombineAlgorithm().name());
        entity.setRoot(domain.isRoot());
        entity.setTenantId(domain.getTenantId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
