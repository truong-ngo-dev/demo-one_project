package vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_other_asset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.*;

import java.util.Set;

public class CreateOtherAsset {

    private static final Set<FixedAssetType> ALLOWED_TYPES = Set.of(
            FixedAssetType.FACILITY,
            FixedAssetType.MEETING_ROOM,
            FixedAssetType.PARKING_SLOT,
            FixedAssetType.COMMON_AREA,
            FixedAssetType.EQUIPMENT
    );

    public record Command(String parentId, String name, String code, int sequenceNo, FixedAssetType type) {
        public Command {
            Assert.hasText(parentId, "parentId is required");
            Assert.hasText(name, "name is required");
            if (!ALLOWED_TYPES.contains(type)) {
                throw new IllegalArgumentException("type must be one of: FACILITY, MEETING_ROOM, PARKING_SLOT, COMMON_AREA, EQUIPMENT");
            }
        }
    }

    public record Result(String assetId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final FixedAssetRepository fixedAssetRepository;

        @Override
        @Transactional
        public Result handle(Command command) {
            FixedAsset parent = fixedAssetRepository.findById(FixedAssetId.of(command.parentId()))
                    .orElseThrow(FixedAssetException::notFound);

            FixedAssetId id = FixedAssetId.generate();
            String path = parent.getPath() + "/" + id.getValue();

            FixedAsset asset = FixedAsset.create(
                    id, command.type(), command.name(), command.code(),
                    command.sequenceNo(), FixedAssetId.of(command.parentId()), path, null
            );
            fixedAssetRepository.save(asset);

            return new Result(asset.getId().getValue());
        }
    }
}
