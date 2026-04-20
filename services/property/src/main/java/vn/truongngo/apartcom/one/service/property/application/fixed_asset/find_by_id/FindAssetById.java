package vn.truongngo.apartcom.one.service.property.application.fixed_asset.find_by_id;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.property.application.fixed_asset.AssetView;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetException;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetId;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetRepository;

public class FindAssetById {

    public record Query(String assetId) {
        public Query {
            Assert.hasText(assetId, "assetId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, AssetView> {

        private final FixedAssetRepository fixedAssetRepository;

        @Override
        public AssetView handle(Query query) {
            return fixedAssetRepository.findById(FixedAssetId.of(query.assetId()))
                    .map(AssetView::from)
                    .orElseThrow(FixedAssetException::notFound);
        }
    }
}
