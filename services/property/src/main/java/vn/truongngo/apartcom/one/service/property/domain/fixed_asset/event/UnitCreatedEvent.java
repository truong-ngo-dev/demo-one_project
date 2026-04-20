package vn.truongngo.apartcom.one.service.property.domain.fixed_asset.event;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetId;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetType;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UnitCreatedEvent extends AbstractDomainEvent implements DomainEvent {

    private final FixedAssetType type;
    private final String buildingId;
    private final String code;

    public UnitCreatedEvent(FixedAssetId unitId, FixedAssetType type, String buildingId, String code) {
        super(UUID.randomUUID().toString(), unitId.getValue(), Instant.now());
        this.type       = type;
        this.buildingId = buildingId;
        this.code       = code;
    }
}
