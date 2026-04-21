package vn.truongngo.apartcom.one.service.admin.application.reference.register_building;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReference;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReferenceRepository;

public class RegisterBuilding {

    public record Command(String buildingId, String name, String managingOrgId) {
        public Command {
            Assert.hasText(buildingId, "buildingId is required");
            Assert.hasText(name, "name is required");
            Assert.hasText(managingOrgId, "managingOrgId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final BuildingReferenceRepository buildingReferenceRepository;

        @Override
        @Transactional
        public Void handle(Command command) {
            buildingReferenceRepository.upsert(
                    BuildingReference.of(command.buildingId(), command.name(), command.managingOrgId()));
            return null;
        }
    }
}
