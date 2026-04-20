package vn.truongngo.apartcom.one.service.property.domain.fixed_asset.event;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetId;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BuildingCreatedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String name;
    private final String managingOrgId;

    public BuildingCreatedEvent(FixedAssetId buildingId, String name, String managingOrgId) {
        super(UUID.randomUUID().toString(), buildingId.getValue(), Instant.now());
        this.name          = name;
        this.managingOrgId = managingOrgId;
    }
}
