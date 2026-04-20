package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.reference;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.reference.OrgReference;
import vn.truongngo.apartcom.one.service.admin.domain.reference.OrgReferenceRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.reference.OrgReferenceJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.reference.OrgReferenceMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrgReferencePersistenceAdapter implements OrgReferenceRepository {

    private final OrgReferenceJpaRepository jpaRepository;

    @Override
    public void upsert(OrgReference ref) {
        jpaRepository.save(OrgReferenceMapper.toEntity(ref));
    }

    @Override
    public boolean existsById(String orgId) {
        return jpaRepository.existsById(orgId);
    }

    @Override
    public Optional<OrgReference> findById(String orgId) {
        return jpaRepository.findById(orgId).map(OrgReferenceMapper::toDomain);
    }
}
