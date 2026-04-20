package vn.truongngo.apartcom.one.service.property.presentation.agreement;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.property.application.agreement.AgreementView;
import vn.truongngo.apartcom.one.service.property.application.agreement.activate.ActivateAgreement;
import vn.truongngo.apartcom.one.service.property.application.agreement.create.CreateOccupancyAgreement;
import vn.truongngo.apartcom.one.service.property.application.agreement.expire.ExpireAgreement;
import vn.truongngo.apartcom.one.service.property.application.agreement.find_by_asset.FindAgreementsByAsset;
import vn.truongngo.apartcom.one.service.property.application.agreement.find_by_building.FindAgreementsByBuilding;
import vn.truongngo.apartcom.one.service.property.application.agreement.find_by_party.FindAgreementsByParty;
import vn.truongngo.apartcom.one.service.property.application.agreement.terminate.TerminateAgreement;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementStatus;
import vn.truongngo.apartcom.one.service.property.presentation.agreement.model.CreateAgreementRequest;
import vn.truongngo.apartcom.one.service.property.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.property.presentation.fixed_asset.FixedAssetController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agreements")
@RequiredArgsConstructor
public class OccupancyAgreementController {

    private final CreateOccupancyAgreement.Handler createHandler;
    private final ActivateAgreement.Handler activateHandler;
    private final TerminateAgreement.Handler terminateHandler;
    private final ExpireAgreement.Handler expireHandler;
    private final FindAgreementsByAsset.Handler findByAssetHandler;
    private final FindAgreementsByParty.Handler findByPartyHandler;
    private final FindAgreementsByBuilding.Handler findByBuildingHandler;

    // UC-007: Tạo agreement
    @PostMapping
    public ResponseEntity<ApiResponse<FixedAssetController.IdResponse>> createAgreement(
            @RequestBody CreateAgreementRequest request) {
        var result = createHandler.handle(new CreateOccupancyAgreement.Command(
                request.partyId(), request.partyType(), request.assetId(),
                request.agreementType(), request.startDate(), request.endDate(), request.contractRef()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(new FixedAssetController.IdResponse(result.agreementId())));
    }

    // UC-008: Activate agreement
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable String id) {
        activateHandler.handle(new ActivateAgreement.Command(id));
        return ResponseEntity.ok(ApiResponse.of(null));
    }

    // UC-009: Terminate agreement
    @PostMapping("/{id}/terminate")
    public ResponseEntity<ApiResponse<Void>> terminate(@PathVariable String id) {
        terminateHandler.handle(new TerminateAgreement.Command(id));
        return ResponseEntity.ok(ApiResponse.of(null));
    }

    // UC-010: Expire agreement
    @PostMapping("/{id}/expire")
    public ResponseEntity<ApiResponse<Void>> expire(@PathVariable String id) {
        expireHandler.handle(new ExpireAgreement.Command(id));
        return ResponseEntity.ok(ApiResponse.of(null));
    }

    // UC-011/012/013: Query agreements by assetId, partyId, or buildingId
    @GetMapping
    public ResponseEntity<ApiResponse<List<AgreementView>>> getAgreements(
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String partyId,
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) OccupancyAgreementStatus status) {
        if (buildingId != null) {
            return ResponseEntity.ok(ApiResponse.of(
                    findByBuildingHandler.handle(new FindAgreementsByBuilding.Query(buildingId, status))));
        }
        if (assetId != null) {
            return ResponseEntity.ok(ApiResponse.of(
                    findByAssetHandler.handle(new FindAgreementsByAsset.Query(assetId, status))));
        }
        if (partyId != null) {
            return ResponseEntity.ok(ApiResponse.of(
                    findByPartyHandler.handle(new FindAgreementsByParty.Query(partyId))));
        }
        throw new IllegalArgumentException("One of buildingId, assetId, or partyId must be provided");
    }
}
