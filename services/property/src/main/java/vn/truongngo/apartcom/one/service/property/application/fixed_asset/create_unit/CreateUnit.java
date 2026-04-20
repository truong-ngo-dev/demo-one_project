package vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_unit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.*;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.event.UnitCreatedEvent;

public class CreateUnit {

    public record Command(String floorId, String name, String code, int sequenceNo, FixedAssetType type) {
        public Command {
            Assert.hasText(floorId, "floorId is required");
            Assert.hasText(name, "name is required");
            if (type != FixedAssetType.RESIDENTIAL_UNIT && type != FixedAssetType.COMMERCIAL_SPACE) {
                throw new IllegalArgumentException("type must be RESIDENTIAL_UNIT or COMMERCIAL_SPACE");
            }
        }
    }

    public record Result(String unitId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final FixedAssetRepository fixedAssetRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            FixedAsset floor = fixedAssetRepository.findById(FixedAssetId.of(command.floorId()))
                    .orElseThrow(FixedAssetException::notFound);

            if (floor.getType() != FixedAssetType.FLOOR) {
                throw FixedAssetException.invalidTypeForParent();
            }

            FixedAssetId id = FixedAssetId.generate();
            String path = floor.getPath() + "/" + id.getValue();

            // extract buildingId: path of floor is /buildingId/floorId → segment [1]
            String buildingId = floor.getPath().split("/")[1];

            FixedAsset unit = FixedAsset.create(
                    id, command.type(), command.name(), command.code(),
                    command.sequenceNo(), FixedAssetId.of(command.floorId()), path, null
            );
            fixedAssetRepository.save(unit);

            eventDispatcher.dispatch(new UnitCreatedEvent(unit.getId(), unit.getType(), buildingId, unit.getCode()));

            return new Result(unit.getId().getValue());
        }
    }
}
