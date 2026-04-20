package vn.truongngo.apartcom.one.service.property.application.fixed_asset.find_tree;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.property.application.fixed_asset.AssetView;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetRepository;

import java.util.List;

public class FindAssetTree {

    public record Query(String buildingId) {
        public Query {
            Assert.hasText(buildingId, "buildingId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<AssetView>> {

        private final FixedAssetRepository fixedAssetRepository;

        @Override
        public List<AssetView> handle(Query query) {
            return fixedAssetRepository.findByPathPrefix("/" + query.buildingId())
                    .stream()
                    .map(AssetView::from)
                    .toList();
        }
    }
}
