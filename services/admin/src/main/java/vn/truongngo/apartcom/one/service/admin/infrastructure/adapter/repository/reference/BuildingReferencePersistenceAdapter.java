package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.reference;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReference;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReferenceRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.reference.BuildingReferenceJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.reference.BuildingReferenceMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BuildingReferencePersistenceAdapter implements BuildingReferenceRepository {

    private final BuildingReferenceJpaRepository jpaRepository;

    @Override
    public void upsert(BuildingReference ref) {
        jpaRepository.save(BuildingReferenceMapper.toEntity(ref));
    }

    @Override
    public boolean existsById(String buildingId) {
        return jpaRepository.existsById(buildingId);
    }

    @Override
    public Optional<BuildingReference> findById(String buildingId) {
        return jpaRepository.findById(buildingId).map(BuildingReferenceMapper::toDomain);
    }
}
