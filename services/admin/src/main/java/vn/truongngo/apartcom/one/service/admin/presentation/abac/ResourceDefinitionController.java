package vn.truongngo.apartcom.one.service.admin.presentation.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.lib.abac.pep.PreEnforce;
import vn.truongngo.apartcom.one.lib.abac.rap.ResourceMapping;
import vn.truongngo.apartcom.one.service.admin.application.resource.command.add_action.AddActionToResource;
import vn.truongngo.apartcom.one.service.admin.application.resource.command.create_resource.CreateResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.application.resource.command.delete_resource.DeleteResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.application.resource.command.remove_action.RemoveActionFromResource;
import vn.truongngo.apartcom.one.service.admin.application.resource.command.update_action.UpdateActionDefinition;
import vn.truongngo.apartcom.one.service.admin.application.resource.command.update_resource.UpdateResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.application.resource.query.get_resource.GetResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.application.resource.query.list_resources.ListResourceDefinitions;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.*;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.admin.presentation.base.PagedApiResponse;

@RestController
@RequestMapping("/api/v1/abac/resources")
@RequiredArgsConstructor
public class ResourceDefinitionController {

    private final CreateResourceDefinition.Handler createHandler;
    private final UpdateResourceDefinition.Handler updateHandler;
    private final DeleteResourceDefinition.Handler deleteHandler;
    private final AddActionToResource.Handler addActionHandler;
    private final UpdateActionDefinition.Handler updateActionHandler;
    private final RemoveActionFromResource.Handler removeActionHandler;
    private final GetResourceDefinition.Handler getHandler;
    private final ListResourceDefinitions.Handler listHandler;

    // UC-019-R1: Tạo resource
    @PostMapping
    @ResourceMapping(resource = "abac_resource", action = "CREATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<CreateResourceDefinition.Result>> create(
            @RequestBody CreateResourceRequest request) {
        CreateResourceDefinition.Result result = createHandler.handle(
                new CreateResourceDefinition.Command(request.name(), request.description(), request.serviceName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    // UC-019-R2: Lấy resource theo ID
    @GetMapping("/{id}")
    @ResourceMapping(resource = "abac_resource", action = "READ")
    @PreEnforce
    public ResponseEntity<ApiResponse<GetResourceDefinition.ResourceView>> getById(@PathVariable Long id) {
        GetResourceDefinition.ResourceView view = getHandler.handle(new GetResourceDefinition.Query(id));
        return ResponseEntity.ok(ApiResponse.of(view));
    }

    // UC-019-R3: Danh sách resources
    @GetMapping
    @ResourceMapping(resource = "abac_resource", action = "LIST")
    @PreEnforce
    public ResponseEntity<PagedApiResponse<ListResourceDefinitions.ResourceSummaryView>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Page<ListResourceDefinitions.ResourceSummaryView> result =
                listHandler.handle(ListResourceDefinitions.Query.of(keyword, page, size));
        return ResponseEntity.ok(PagedApiResponse.of(result));
    }

    // UC-019-R4: Cập nhật resource
    @PutMapping("/{id}")
    @ResourceMapping(resource = "abac_resource", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<UpdateResourceDefinition.Result>> update(
            @PathVariable Long id,
            @RequestBody UpdateResourceRequest request) {
        UpdateResourceDefinition.Result result = updateHandler.handle(
                new UpdateResourceDefinition.Command(id, request.description(), request.serviceName()));
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    // UC-019-R5: Xóa resource
    @DeleteMapping("/{id}")
    @ResourceMapping(resource = "abac_resource", action = "DELETE")
    @PreEnforce
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteHandler.handle(new DeleteResourceDefinition.Command(id));
        return ResponseEntity.noContent().build();
    }

    // UC-019-A1: Thêm action
    @PostMapping("/{resourceId}/actions")
    @ResourceMapping(resource = "abac_resource", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<AddActionToResource.Result>> addAction(
            @PathVariable Long resourceId,
            @RequestBody AddActionRequest request) {
        AddActionToResource.Result result = addActionHandler.handle(
                new AddActionToResource.Command(resourceId, request.name(), request.description(), request.isStandard()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    // UC-019-A2: Cập nhật action
    @PatchMapping("/{resourceId}/actions/{actionId}")
    @ResourceMapping(resource = "abac_resource", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<UpdateActionDefinition.Result>> updateAction(
            @PathVariable Long resourceId,
            @PathVariable Long actionId,
            @RequestBody UpdateActionRequest request) {
        UpdateActionDefinition.Result result = updateActionHandler.handle(
                new UpdateActionDefinition.Command(resourceId, actionId, request.description()));
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    // UC-019-A3: Xóa action
    @DeleteMapping("/{resourceId}/actions/{actionId}")
    @ResourceMapping(resource = "abac_resource", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<Void> removeAction(
            @PathVariable Long resourceId,
            @PathVariable Long actionId) {
        removeActionHandler.handle(new RemoveActionFromResource.Command(resourceId, actionId));
        return ResponseEntity.noContent().build();
    }
}
