package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy_set.PolicySetJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy_set.PolicySetJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy_set.PolicySetMapper;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PolicySetPersistenceAdapter implements PolicySetRepository {

    private final PolicySetJpaRepository jpaRepository;

    @Override
    public PolicySetDefinition save(PolicySetDefinition policySet) {
        PolicySetJpaEntity entity = PolicySetMapper.toEntity(policySet);
        PolicySetJpaEntity saved = jpaRepository.save(entity);
        return PolicySetMapper.toDomain(saved);
    }

    @Override
    public Optional<PolicySetDefinition> findById(PolicySetId id) {
        return jpaRepository.findById(id.getValue())
                .map(PolicySetMapper::toDomain);
    }

    @Override
    public Page<PolicySetDefinition> findAll(String keyword, Pageable pageable) {
        return jpaRepository.search(keyword, pageable)
                .map(PolicySetMapper::toDomain);
    }

    @Override
    public void delete(PolicySetId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsPoliciesFor(PolicySetId id) {
        return jpaRepository.existsPoliciesByPolicySetId(id.getValue());
    }

    @Override
    public List<PolicySetDefinition> findAllRoot() {
        return jpaRepository.findAllByIsRootTrue().stream()
                .map(PolicySetMapper::toDomain)
                .toList();
    }
}
