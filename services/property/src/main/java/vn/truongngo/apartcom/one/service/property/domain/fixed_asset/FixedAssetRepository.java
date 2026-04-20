package vn.truongngo.apartcom.one.service.property.domain.fixed_asset;

import vn.truongngo.apartcom.one.lib.common.domain.service.Repository;

import java.util.List;

public interface FixedAssetRepository extends Repository<FixedAsset, FixedAssetId> {

    List<FixedAsset> findByPathPrefix(String pathPrefix);
}
