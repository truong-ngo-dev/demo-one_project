package vn.truongngo.apartcom.one.service.admin.presentation.reference;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.admin.application.reference.get_all_buildings.GetAllBuildings;
import vn.truongngo.apartcom.one.service.admin.application.reference.register_building.RegisterBuilding;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buildings")
@RequiredArgsConstructor
public class BuildingReferenceController {

    private final GetAllBuildings.Handler getAllBuildingsHandler;
    private final RegisterBuilding.Handler registerBuildingHandler;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GetAllBuildings.BuildingView>>> getAllBuildings() {
        List<GetAllBuildings.BuildingView> result = getAllBuildingsHandler.handle(new GetAllBuildings.Query());
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    @PostMapping
    public ResponseEntity<Void> registerBuilding(@RequestBody RegisterBuildingRequest req) {
        registerBuildingHandler.handle(new RegisterBuilding.Command(req.buildingId(), req.name(), req.managingOrgId()));
        return ResponseEntity.noContent().build();
    }

    public record RegisterBuildingRequest(String buildingId, String name, String managingOrgId) {}
}
