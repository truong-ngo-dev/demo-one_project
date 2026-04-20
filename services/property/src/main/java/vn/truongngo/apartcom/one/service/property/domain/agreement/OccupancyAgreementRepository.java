package vn.truongngo.apartcom.one.service.property.domain.agreement;

import java.util.List;
import java.util.Optional;

public interface OccupancyAgreementRepository {

    Optional<OccupancyAgreement> findById(OccupancyAgreementId id);

    boolean existsActiveByAssetIdAndType(String assetId, OccupancyAgreementType type);

    List<OccupancyAgreement> findByAssetId(String assetId);

    List<OccupancyAgreement> findByPartyId(String partyId);

    List<OccupancyAgreement> findByAssetIds(List<String> assetIds);

    OccupancyAgreement save(OccupancyAgreement agreement);
}
