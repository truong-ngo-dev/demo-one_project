package vn.truongngo.apartcom.one.service.admin.presentation.role;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.lib.abac.pep.PreEnforce;
import vn.truongngo.apartcom.one.lib.abac.rap.ResourceMapping;
import vn.truongngo.apartcom.one.service.admin.application.role.create.CreateRole;
import vn.truongngo.apartcom.one.service.admin.application.role.delete.DeleteRole;
import vn.truongngo.apartcom.one.service.admin.application.role.find_all.FindAllRoles;
import vn.truongngo.apartcom.one.service.admin.application.role.find_by_id.FindRoleById;
import vn.truongngo.apartcom.one.service.admin.application.role.update.UpdateRole;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.admin.presentation.base.PagedApiResponse;
import vn.truongngo.apartcom.one.service.admin.presentation.role.model.CreateRoleRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.role.model.UpdateRoleRequest;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final CreateRole.Handler createRoleHandler;
    private final FindRoleById.Handler findRoleByIdHandler;
    private final FindAllRoles.Handler findAllRolesHandler;
    private final UpdateRole.Handler updateRoleHandler;
    private final DeleteRole.Handler deleteRoleHandler;

    // UC-008: Tạo role
    @PostMapping
    @ResourceMapping(resource = "role", action = "CREATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<CreateRole.Result>> createRole(@RequestBody CreateRoleRequest request) {
        CreateRole.Result result = createRoleHandler.handle(new CreateRole.Command(request.name(), request.description(), request.scope()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    // UC-009: Tìm role theo ID
    @GetMapping("/{id}")
    @ResourceMapping(resource = "role", action = "READ")
    @PreEnforce
    public ResponseEntity<ApiResponse<FindRoleById.RoleDetail>> getRoleById(@PathVariable String id) {
        FindRoleById.RoleDetail detail = findRoleByIdHandler.handle(new FindRoleById.Query(id));
        return ResponseEntity.ok(ApiResponse.of(detail));
    }

    // UC-010: Danh sách roles
    @GetMapping
    @ResourceMapping(resource = "role", action = "LIST")
    @PreEnforce
    public ResponseEntity<PagedApiResponse<FindAllRoles.RoleSummary>> getAllRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        FindAllRoles.Query query = FindAllRoles.Query.of(keyword, page, size, sort);
        Page<FindAllRoles.RoleSummary> result = findAllRolesHandler.handle(query);
        return ResponseEntity.ok(PagedApiResponse.of(result));
    }

    // UC-011: Cập nhật role
    @PatchMapping("/{id}")
    @ResourceMapping(resource = "role", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<UpdateRole.RoleDetail>> updateRole(
            @PathVariable String id,
            @RequestBody UpdateRoleRequest request) {
        UpdateRole.RoleDetail detail = updateRoleHandler.handle(
                new UpdateRole.Command(id, request.name(), request.description()));
        return ResponseEntity.ok(ApiResponse.of(detail));
    }

    // UC-012: Xóa role
    @DeleteMapping("/{id}")
    @ResourceMapping(resource = "role", action = "DELETE")
    @PreEnforce
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        deleteRoleHandler.handle(new DeleteRole.Command(id));
        return ResponseEntity.noContent().build();
    }
}
