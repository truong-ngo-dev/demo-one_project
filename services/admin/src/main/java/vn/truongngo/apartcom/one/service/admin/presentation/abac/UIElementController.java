package vn.truongngo.apartcom.one.service.admin.presentation.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.lib.abac.pep.PreEnforce;
import vn.truongngo.apartcom.one.lib.abac.rap.ResourceMapping;
import vn.truongngo.apartcom.one.service.admin.application.ui_element.command.create_ui_element.CreateUIElement;
import vn.truongngo.apartcom.one.service.admin.application.ui_element.command.delete_ui_element.DeleteUIElement;
import vn.truongngo.apartcom.one.service.admin.application.ui_element.command.update_ui_element.UpdateUIElement;
import vn.truongngo.apartcom.one.service.admin.application.ui_element.query.evaluate.EvaluateUIElements;
import vn.truongngo.apartcom.one.service.admin.application.ui_element.query.get_ui_element.GetUIElement;
import vn.truongngo.apartcom.one.service.admin.application.ui_element.query.list_ui_elements.ListUIElements;
import vn.truongngo.apartcom.one.service.admin.application.ui_element.query.list_uncovered_ui_elements.ListUncoveredUIElements;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.CreateUIElementRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.EvaluateUIElementsRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.UpdateUIElementRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.admin.presentation.base.PagedApiResponse;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/abac/ui-elements")
@RequiredArgsConstructor
public class UIElementController {

    private final CreateUIElement.Handler createHandler;
    private final UpdateUIElement.Handler updateHandler;
    private final DeleteUIElement.Handler deleteHandler;
    private final GetUIElement.Handler getHandler;
    private final ListUIElements.Handler listHandler;
    private final EvaluateUIElements.Handler evaluateHandler;
    private final ListUncoveredUIElements.Handler listUncoveredHandler;

    @PostMapping
    @ResourceMapping(resource = "abac_ui_element", action = "CREATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<CreateUIElement.Result>> create(
            @RequestBody CreateUIElementRequest request) {
        CreateUIElement.Result result = createHandler.handle(new CreateUIElement.Command(
                request.elementId(), request.label(), request.type(),
                request.group(), request.orderIndex(), request.resourceId(), request.actionId(),
                request.scope()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    @GetMapping("/{id}")
    @ResourceMapping(resource = "abac_ui_element", action = "READ")
    @PreEnforce
    public ResponseEntity<ApiResponse<GetUIElement.UIElementView>> getById(@PathVariable Long id) {
        GetUIElement.UIElementView view = getHandler.handle(new GetUIElement.Query(id));
        return ResponseEntity.ok(ApiResponse.of(view));
    }

    @GetMapping
    @ResourceMapping(resource = "abac_ui_element", action = "LIST")
    @PreEnforce
    public ResponseEntity<PagedApiResponse<ListUIElements.UIElementSummary>> list(
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Page<ListUIElements.UIElementSummary> result =
                listHandler.handle(ListUIElements.Query.of(resourceId, type, group, scope, page, size));
        return ResponseEntity.ok(PagedApiResponse.of(result));
    }

    @PutMapping("/{id}")
    @ResourceMapping(resource = "abac_ui_element", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestBody UpdateUIElementRequest request) {
        updateHandler.handle(new UpdateUIElement.Command(
                id, request.label(), request.type(), request.group(),
                request.orderIndex(), request.resourceId(), request.actionId(),
                request.scope()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @ResourceMapping(resource = "abac_ui_element", action = "DELETE")
    @PreEnforce
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteHandler.handle(new DeleteUIElement.Command(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/uncovered")
    @ResourceMapping(resource = "abac_ui_element", action = "READ")
    @PreEnforce
    public ResponseEntity<ApiResponse<List<ListUncoveredUIElements.UncoveredUIElement>>> listUncovered() {
        List<ListUncoveredUIElements.UncoveredUIElement> result =
                listUncoveredHandler.handle(new ListUncoveredUIElements.Query());
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<EvaluateUIElements.Result>> evaluate(
            @RequestBody EvaluateUIElementsRequest request,
            Principal principal) {
        EvaluateUIElements.Result result = evaluateHandler.handle(
                new EvaluateUIElements.Query(request.elementIds(), principal));
        return ResponseEntity.ok(ApiResponse.of(result));
    }
}
