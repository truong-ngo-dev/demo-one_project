package vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_floor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.*;

public class CreateFloor {

    public record Command(String buildingId, String name, String code, int sequenceNo) {
        public Command {
            Assert.hasText(buildingId, "buildingId is required");
            Assert.hasText(name, "name is required");
        }
    }

    public record Result(String floorId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final FixedAssetRepository fixedAssetRepository;

        @Override
        @Transactional
        public Result handle(Command command) {
            FixedAsset building = fixedAssetRepository.findById(FixedAssetId.of(command.buildingId()))
                    .orElseThrow(FixedAssetException::notFound);

            if (building.getType() != FixedAssetType.BUILDING) {
                throw FixedAssetException.invalidTypeForParent();
            }

            FixedAssetId id = FixedAssetId.generate();
            String path = building.getPath() + "/" + id.getValue();

            FixedAsset floor = FixedAsset.create(
                    id, FixedAssetType.FLOOR, command.name(), command.code(),
                    command.sequenceNo(), FixedAssetId.of(command.buildingId()), path, null
            );
            fixedAssetRepository.save(floor);

            return new Result(floor.getId().getValue());
        }
    }
}
