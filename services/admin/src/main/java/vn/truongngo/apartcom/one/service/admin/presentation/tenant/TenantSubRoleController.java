package vn.truongngo.apartcom.one.service.admin.presentation.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.admin.application.tenant.assign_sub_role.AssignSubRole;
import vn.truongngo.apartcom.one.service.admin.application.tenant.find_sub_roles_by_org.FindSubRolesByOrg;
import vn.truongngo.apartcom.one.service.admin.application.tenant.revoke_sub_role.RevokeSubRole;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRole;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.admin.presentation.tenant.model.AssignSubRoleRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{orgId}/sub-roles")
@RequiredArgsConstructor
public class TenantSubRoleController {

    private final AssignSubRole.Handler assignSubRoleHandler;
    private final RevokeSubRole.Handler revokeSubRoleHandler;
    private final FindSubRolesByOrg.Handler findSubRolesByOrgHandler;

    @PostMapping
    public ResponseEntity<ApiResponse<AssignSubRole.Result>> assignSubRole(
            @PathVariable String orgId,
            @RequestBody AssignSubRoleRequest request) {
        AssignSubRole.Result result = assignSubRoleHandler.handle(
                new AssignSubRole.Command(request.userId(), orgId, request.subRole(), request.assignedBy()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    @DeleteMapping("/{userId}/{subRole}")
    public ResponseEntity<ApiResponse<Void>> revokeSubRole(
            @PathVariable String orgId,
            @PathVariable String userId,
            @PathVariable TenantSubRole subRole) {
        revokeSubRoleHandler.handle(new RevokeSubRole.Command(userId, orgId, subRole));
        return ResponseEntity.ok(ApiResponse.of(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FindSubRolesByOrg.SubRoleView>>> findSubRoles(
            @PathVariable String orgId) {
        List<FindSubRolesByOrg.SubRoleView> result = findSubRolesByOrgHandler.handle(
                new FindSubRolesByOrg.Query(orgId));
        return ResponseEntity.ok(ApiResponse.of(result));
    }
}
