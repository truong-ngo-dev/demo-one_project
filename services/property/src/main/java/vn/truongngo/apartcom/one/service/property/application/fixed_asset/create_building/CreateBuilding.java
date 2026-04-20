package vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_building;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAsset;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetId;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetRepository;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetType;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.event.BuildingCreatedEvent;

public class CreateBuilding {

    public record Command(String name, String managingOrgId) {
        public Command {
            Assert.hasText(name, "name is required");
            Assert.hasText(managingOrgId, "managingOrgId is required");
        }
    }

    public record Result(String buildingId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final FixedAssetRepository fixedAssetRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            FixedAssetId id = FixedAssetId.generate();
            String path = "/" + id.getValue();

            FixedAsset building = FixedAsset.create(
                    id, FixedAssetType.BUILDING, command.name(), null, 0, null, path, command.managingOrgId()
            );
            fixedAssetRepository.save(building);

            eventDispatcher.dispatch(new BuildingCreatedEvent(building.getId(), building.getName(), building.getManagingOrgId()));

            return new Result(building.getId().getValue());
        }
    }
}
