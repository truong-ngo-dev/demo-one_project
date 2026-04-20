package vn.truongngo.apartcom.one.service.property.infrastructure.adapter.repository.agreement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.property.domain.agreement.*;
import vn.truongngo.apartcom.one.service.property.infrastructure.persistence.agreement.OccupancyAgreementJpaEntity;
import vn.truongngo.apartcom.one.service.property.infrastructure.persistence.agreement.OccupancyAgreementJpaRepository;
import vn.truongngo.apartcom.one.service.property.infrastructure.persistence.agreement.OccupancyAgreementMapper;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OccupancyAgreementPersistenceAdapter implements OccupancyAgreementRepository {

    private final OccupancyAgreementJpaRepository jpaRepository;

    @Override
    public Optional<OccupancyAgreement> findById(OccupancyAgreementId id) {
        return jpaRepository.findById(id.getValue()).map(OccupancyAgreementMapper::toDomain);
    }

    @Override
    public boolean existsActiveByAssetIdAndType(String assetId, OccupancyAgreementType type) {
        return jpaRepository.existsByAssetIdAndAgreementTypeAndStatus(
                assetId, type, OccupancyAgreementStatus.ACTIVE);
    }

    @Override
    public List<OccupancyAgreement> findByAssetId(String assetId) {
        return jpaRepository.findAllByAssetId(assetId)
                .stream()
                .map(OccupancyAgreementMapper::toDomain)
                .toList();
    }

    @Override
    public List<OccupancyAgreement> findByPartyId(String partyId) {
        return jpaRepository.findAllByPartyId(partyId)
                .stream()
                .map(OccupancyAgreementMapper::toDomain)
                .toList();
    }

    @Override
    public List<OccupancyAgreement> findByAssetIds(List<String> assetIds) {
        if (assetIds.isEmpty()) return List.of();
        return jpaRepository.findAllByAssetIdIn(assetIds)
                .stream()
                .map(OccupancyAgreementMapper::toDomain)
                .toList();
    }

    @Override
    public OccupancyAgreement save(OccupancyAgreement agreement) {
        Optional<OccupancyAgreementJpaEntity> existing = jpaRepository.findById(agreement.getId().getValue());
        if (existing.isPresent()) {
            OccupancyAgreementMapper.updateEntity(existing.get(), agreement);
            return OccupancyAgreementMapper.toDomain(jpaRepository.save(existing.get()));
        } else {
            return OccupancyAgreementMapper.toDomain(jpaRepository.save(OccupancyAgreementMapper.toEntity(agreement)));
        }
    }
}
