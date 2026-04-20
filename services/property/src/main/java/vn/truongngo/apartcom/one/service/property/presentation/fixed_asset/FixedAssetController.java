package vn.truongngo.apartcom.one.service.property.presentation.fixed_asset;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.property.application.fixed_asset.AssetView;
import vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_building.CreateBuilding;
import vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_floor.CreateFloor;
import vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_other_asset.CreateOtherAsset;
import vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_unit.CreateUnit;
import vn.truongngo.apartcom.one.service.property.application.fixed_asset.find_by_id.FindAssetById;
import vn.truongngo.apartcom.one.service.property.application.fixed_asset.find_tree.FindAssetTree;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetType;
import vn.truongngo.apartcom.one.service.property.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.property.presentation.fixed_asset.model.CreateAssetRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class FixedAssetController {

    private final CreateBuilding.Handler createBuildingHandler;
    private final CreateFloor.Handler createFloorHandler;
    private final CreateUnit.Handler createUnitHandler;
    private final CreateOtherAsset.Handler createOtherAssetHandler;
    private final FindAssetById.Handler findAssetByIdHandler;
    private final FindAssetTree.Handler findAssetTreeHandler;

    // UC-001/002/003/004: Tạo asset — dispatch theo type
    @PostMapping
    public ResponseEntity<ApiResponse<IdResponse>> createAsset(@RequestBody CreateAssetRequest request) {
        String assetId = switch (request.type()) {
            case BUILDING -> createBuildingHandler.handle(
                    new CreateBuilding.Command(request.name(), request.managingOrgId())
            ).buildingId();
            case FLOOR -> createFloorHandler.handle(
                    new CreateFloor.Command(request.parentId(), request.name(), request.code(), request.sequenceNo())
            ).floorId();
            case RESIDENTIAL_UNIT, COMMERCIAL_SPACE -> createUnitHandler.handle(
                    new CreateUnit.Command(request.parentId(), request.name(), request.code(), request.sequenceNo(), request.type())
            ).unitId();
            default -> createOtherAssetHandler.handle(
                    new CreateOtherAsset.Command(request.parentId(), request.name(), request.code(), request.sequenceNo(), request.type())
            ).assetId();
        };
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(new IdResponse(assetId)));
    }

    // UC-006: Tìm asset theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetView>> getAssetById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.of(findAssetByIdHandler.handle(new FindAssetById.Query(id))));
    }

    // UC-005: Xem cây tài sản
    @GetMapping
    public ResponseEntity<ApiResponse<List<AssetView>>> getAssetTree(@RequestParam String buildingId) {
        return ResponseEntity.ok(ApiResponse.of(findAssetTreeHandler.handle(new FindAssetTree.Query(buildingId))));
    }

    public record IdResponse(String id) {}
}
