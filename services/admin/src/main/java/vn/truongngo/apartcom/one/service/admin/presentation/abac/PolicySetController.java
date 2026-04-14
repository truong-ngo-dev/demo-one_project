package vn.truongngo.apartcom.one.service.admin.presentation.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName;
import vn.truongngo.apartcom.one.lib.abac.pep.PreEnforce;
import vn.truongngo.apartcom.one.lib.abac.rap.ResourceMapping;
import vn.truongngo.apartcom.one.service.admin.application.policy_set.command.create_policy_set.CreatePolicySet;
import vn.truongngo.apartcom.one.service.admin.application.policy_set.command.delete_policy_set.DeletePolicySet;
import vn.truongngo.apartcom.one.service.admin.application.policy_set.command.update_policy_set.UpdatePolicySet;
import vn.truongngo.apartcom.one.service.admin.application.policy_set.query.delete_preview.GetPolicySetDeletePreview;
import vn.truongngo.apartcom.one.service.admin.application.policy_set.query.get_policy_set.GetPolicySet;
import vn.truongngo.apartcom.one.service.admin.application.policy_set.query.list_policy_sets.ListPolicySets;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Scope;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.admin.presentation.base.PagedApiResponse;

@RestController
@RequestMapping("/api/v1/abac/policy-sets")
@RequiredArgsConstructor
public class PolicySetController {

    private final CreatePolicySet.Handler createHandler;
    private final UpdatePolicySet.Handler updateHandler;
    private final DeletePolicySet.Handler deleteHandler;
    private final GetPolicySet.Handler getHandler;
    private final ListPolicySets.Handler listHandler;
    private final GetPolicySetDeletePreview.Handler deletePreviewHandler;

    @GetMapping
    @ResourceMapping(resource = "abac_policy_set", action = "LIST")
    @PreEnforce
    public ResponseEntity<PagedApiResponse<ListPolicySets.PolicySetSummary>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<ListPolicySets.PolicySetSummary> result = listHandler.handle(
                new ListPolicySets.Query(keyword, pageable));
        return ResponseEntity.ok(PagedApiResponse.of(result));
    }

    @GetMapping("/{id}")
    @ResourceMapping(resource = "abac_policy_set", action = "READ")
    @PreEnforce
    public ResponseEntity<ApiResponse<GetPolicySet.Result>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(getHandler.handle(new GetPolicySet.Query(id))));
    }

    @PostMapping
    @ResourceMapping(resource = "abac_policy_set", action = "CREATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<CreatePolicySet.Result>> create(
            @RequestBody PolicySetRequest request) {
        CreatePolicySet.Result result = createHandler.handle(new CreatePolicySet.Command(
                request.name(), Scope.valueOf(request.scope()),
                CombineAlgorithmName.valueOf(request.combineAlgorithm()),
                request.isRoot(), request.tenantId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    @PutMapping("/{id}")
    @ResourceMapping(resource = "abac_policy_set", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<Void> update(@PathVariable Long id,
                                        @RequestBody PolicySetRequest request) {
        updateHandler.handle(new UpdatePolicySet.Command(id,
                Scope.valueOf(request.scope()),
                CombineAlgorithmName.valueOf(request.combineAlgorithm()),
                request.isRoot(), request.tenantId()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/delete-preview")
    @ResourceMapping(resource = "abac_policy_set", action = "READ")
    @PreEnforce
    public ResponseEntity<ApiResponse<GetPolicySetDeletePreview.Result>> deletePreview(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(deletePreviewHandler.handle(
                new GetPolicySetDeletePreview.Query(id))));
    }

    @DeleteMapping("/{id}")
    @ResourceMapping(resource = "abac_policy_set", action = "DELETE")
    @PreEnforce
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteHandler.handle(new DeletePolicySet.Command(id));
        return ResponseEntity.noContent().build();
    }

    record PolicySetRequest(String name, String scope, String combineAlgorithm,
                            boolean isRoot, String tenantId) {}
}
