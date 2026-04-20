package vn.truongngo.apartcom.one.service.admin.application.event.building;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReference;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReferenceRepository;

@Component
@RequiredArgsConstructor
public class BuildingCreatedEventHandler {

    private final BuildingReferenceRepository buildingReferenceRepository;

    @Transactional
    public void handle(BuildingCreatedPayload payload) {
        buildingReferenceRepository.upsert(
                BuildingReference.of(payload.buildingId(), payload.name(), payload.managingOrgId()));
    }

    public record BuildingCreatedPayload(String buildingId, String name, String managingOrgId) {}
}
