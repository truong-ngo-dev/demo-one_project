package vn.truongngo.apartcom.one.service.property.application.agreement.find_by_building;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.property.application.agreement.AgreementView;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementRepository;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementStatus;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetException;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetId;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetRepository;

import java.util.List;
import java.util.stream.Stream;

public class FindAgreementsByBuilding {

    public record Query(String buildingId, OccupancyAgreementStatus status) {
        public Query {
            Assert.hasText(buildingId, "buildingId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<AgreementView>> {

        private final FixedAssetRepository fixedAssetRepository;
        private final OccupancyAgreementRepository agreementRepository;

        @Override
        public List<AgreementView> handle(Query query) {
            var building = fixedAssetRepository.findById(FixedAssetId.of(query.buildingId()))
                    .orElseThrow(FixedAssetException::notFound);

            var children = fixedAssetRepository.findByPathPrefix(building.getPath());

            List<String> allAssetIds = Stream.concat(
                    Stream.of(query.buildingId()),
                    children.stream().map(a -> a.getId().getValue())
            ).toList();

            return agreementRepository.findByAssetIds(allAssetIds)
                    .stream()
                    .filter(a -> query.status() == null || a.getStatus() == query.status())
                    .map(AgreementView::from)
                    .toList();
        }
    }
}
