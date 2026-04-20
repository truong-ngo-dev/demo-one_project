package vn.truongngo.apartcom.one.service.property.domain.fixed_asset;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

import java.util.UUID;

public class FixedAssetId extends AbstractId<String> implements Id<String> {

    private FixedAssetId(String value) {
        super(value);
    }

    public static FixedAssetId of(String value) {
        return new FixedAssetId(value);
    }

    public static FixedAssetId generate() {
        return new FixedAssetId(UUID.randomUUID().toString());
    }
}
