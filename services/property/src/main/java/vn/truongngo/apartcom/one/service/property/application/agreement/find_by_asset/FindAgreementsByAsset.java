package vn.truongngo.apartcom.one.service.property.application.agreement.find_by_asset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.property.application.agreement.AgreementView;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementRepository;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementStatus;

import java.util.List;

public class FindAgreementsByAsset {

    public record Query(String assetId, OccupancyAgreementStatus status) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<AgreementView>> {

        private final OccupancyAgreementRepository agreementRepository;

        @Override
        public List<AgreementView> handle(Query query) {
            return agreementRepository.findByAssetId(query.assetId())
                    .stream()
                    .filter(a -> query.status() == null || a.getStatus() == query.status())
                    .map(AgreementView::from)
                    .toList();
        }
    }
}
