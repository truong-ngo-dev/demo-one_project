package vn.truongngo.apartcom.one.service.admin.presentation.operator;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.admin.application.operator.assign_operator_context.AssignOperatorContext;
import vn.truongngo.apartcom.one.service.admin.application.operator.assign_roles_to_operator.AssignRolesToOperatorContext;
import vn.truongngo.apartcom.one.service.admin.application.operator.find_operators_by_building.FindOperatorsByBuilding;
import vn.truongngo.apartcom.one.service.admin.application.operator.link_party_id.LinkPartyId;
import vn.truongngo.apartcom.one.service.admin.application.operator.revoke_operator_context.RevokeOperatorContext;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.admin.presentation.operator.model.AssignOperatorContextRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.operator.model.AssignRolesToOperatorRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.operator.model.LinkPartyIdRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operators")
@RequiredArgsConstructor
public class OperatorContextController {

    private final LinkPartyId.Handler linkPartyIdHandler;
    private final AssignOperatorContext.Handler assignOperatorContextHandler;
    private final RevokeOperatorContext.Handler revokeOperatorContextHandler;
    private final FindOperatorsByBuilding.Handler findOperatorsByBuildingHandler;
    private final AssignRolesToOperatorContext.Handler assignRolesToOperatorHandler;

    @PostMapping("/link-party")
    public ResponseEntity<ApiResponse<Void>> linkParty(@RequestBody LinkPartyIdRequest request) {
        linkPartyIdHandler.handle(new LinkPartyId.Command(request.userId(), request.partyId()));
        return ResponseEntity.ok(ApiResponse.of(null));
    }

    @PostMapping("/{buildingId}/assign")
    public ResponseEntity<ApiResponse<Void>> assignContext(
            @PathVariable String buildingId,
            @RequestBody AssignOperatorContextRequest request) {
        assignOperatorContextHandler.handle(
                new AssignOperatorContext.Command(request.userId(), buildingId, request.roleIds()));
        return ResponseEntity.ok(ApiResponse.of(null));
    }

    @DeleteMapping("/{buildingId}/revoke/{userId}")
    public ResponseEntity<ApiResponse<Void>> revokeContext(
            @PathVariable String buildingId,
            @PathVariable String userId) {
        revokeOperatorContextHandler.handle(new RevokeOperatorContext.Command(userId, buildingId));
        return ResponseEntity.ok(ApiResponse.of(null));
    }

    @GetMapping("/{buildingId}")
    public ResponseEntity<ApiResponse<List<FindOperatorsByBuilding.OperatorView>>> findOperators(
            @PathVariable String buildingId) {
        List<FindOperatorsByBuilding.OperatorView> result = findOperatorsByBuildingHandler.handle(
                new FindOperatorsByBuilding.Query(buildingId));
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    @PutMapping("/{buildingId}/roles/{userId}")
    public ResponseEntity<ApiResponse<Void>> assignRoles(
            @PathVariable String buildingId,
            @PathVariable String userId,
            @RequestBody AssignRolesToOperatorRequest request) {
        assignRolesToOperatorHandler.handle(
                new AssignRolesToOperatorContext.Command(userId, buildingId, request.roleIds()));
        return ResponseEntity.ok(ApiResponse.of(null));
    }
}
