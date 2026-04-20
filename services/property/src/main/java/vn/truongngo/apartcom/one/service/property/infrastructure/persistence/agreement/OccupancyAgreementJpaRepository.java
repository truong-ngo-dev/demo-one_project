package vn.truongngo.apartcom.one.service.property.infrastructure.persistence.agreement;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementStatus;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementType;

import java.util.List;

public interface OccupancyAgreementJpaRepository extends JpaRepository<OccupancyAgreementJpaEntity, String> {

    boolean existsByAssetIdAndAgreementTypeAndStatus(
            String assetId, OccupancyAgreementType agreementType, OccupancyAgreementStatus status);

    List<OccupancyAgreementJpaEntity> findAllByAssetId(String assetId);

    List<OccupancyAgreementJpaEntity> findAllByPartyId(String partyId);

    List<OccupancyAgreementJpaEntity> findAllByAssetIdIn(List<String> assetIds);
}
