package vn.truongngo.apartcom.one.service.property.infrastructure.persistence.agreement;

import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreement;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementId;

public class OccupancyAgreementMapper {

    public static OccupancyAgreement toDomain(OccupancyAgreementJpaEntity entity) {
        return OccupancyAgreement.reconstitute(
                OccupancyAgreementId.of(entity.getId()),
                entity.getPartyId(),
                entity.getPartyType(),
                entity.getAssetId(),
                entity.getAgreementType(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getContractRef(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static OccupancyAgreementJpaEntity toEntity(OccupancyAgreement domain) {
        OccupancyAgreementJpaEntity entity = new OccupancyAgreementJpaEntity();
        entity.setId(domain.getId().getValue());
        entity.setPartyId(domain.getPartyId());
        entity.setPartyType(domain.getPartyType());
        entity.setAssetId(domain.getAssetId());
        entity.setAgreementType(domain.getAgreementType());
        entity.setStatus(domain.getStatus());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setContractRef(domain.getContractRef());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static void updateEntity(OccupancyAgreementJpaEntity existing, OccupancyAgreement domain) {
        existing.setStatus(domain.getStatus());
        existing.setUpdatedAt(domain.getUpdatedAt());
    }
}
