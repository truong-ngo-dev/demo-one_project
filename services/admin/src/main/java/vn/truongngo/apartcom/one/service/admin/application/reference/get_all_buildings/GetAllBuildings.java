package vn.truongngo.apartcom.one.service.admin.application.reference.get_all_buildings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReferenceRepository;

import java.util.List;

public class GetAllBuildings {

    public record Query() {}

    public record BuildingView(
            String buildingId,
            String name,
            String managingOrgId
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<BuildingView>> {

        private final BuildingReferenceRepository buildingReferenceRepository;

        @Override
        public List<BuildingView> handle(Query query) {
            return buildingReferenceRepository.findAll().stream()
                    .map(ref -> new BuildingView(ref.getBuildingId(), ref.getName(), ref.getManagingOrgId()))
                    .toList();
        }
    }
}
