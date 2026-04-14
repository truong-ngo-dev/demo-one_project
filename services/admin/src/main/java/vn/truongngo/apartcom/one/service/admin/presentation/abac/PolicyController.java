package vn.truongngo.apartcom.one.service.admin.presentation.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName;
import vn.truongngo.apartcom.one.lib.abac.pep.PreEnforce;
import vn.truongngo.apartcom.one.lib.abac.rap.ResourceMapping;
import vn.truongngo.apartcom.one.service.admin.application.policy.create_policy.CreatePolicy;
import vn.truongngo.apartcom.one.service.admin.application.policy.delete_policy.DeletePolicy;
import vn.truongngo.apartcom.one.service.admin.application.policy.update_policy.UpdatePolicy;
import vn.truongngo.apartcom.one.service.admin.application.policy.delete_preview.GetPolicyDeletePreview;
import vn.truongngo.apartcom.one.service.admin.application.policy.get_policy.GetPolicy;
import vn.truongngo.apartcom.one.service.admin.application.policy.list_policies.ListPolicies;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/abac/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final CreatePolicy.Handler createHandler;
    private final UpdatePolicy.Handler updateHandler;
    private final DeletePolicy.Handler deleteHandler;
    private final GetPolicy.Handler getHandler;
    private final ListPolicies.Handler listHandler;
    private final GetPolicyDeletePreview.Handler deletePreviewHandler;

    @GetMapping
    @ResourceMapping(resource = "abac_policy", action = "LIST")
    @PreEnforce
    public ResponseEntity<ApiResponse<List<ListPolicies.PolicySummary>>> listByPolicySet(
            @RequestParam Long policySetId) {
        return ResponseEntity.ok(ApiResponse.of(
                listHandler.handle(new ListPolicies.Query(policySetId))));
    }

    @GetMapping("/{id}")
    @ResourceMapping(resource = "abac_policy", action = "READ")
    @PreEnforce
    public ResponseEntity<ApiResponse<GetPolicy.Result>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(getHandler.handle(new GetPolicy.Query(id))));
    }

    @PostMapping
    @ResourceMapping(resource = "abac_policy", action = "CREATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<CreatePolicy.Result>> create(
            @RequestBody PolicyRequest request) {
        CreatePolicy.Result result = createHandler.handle(new CreatePolicy.Command(
                request.policySetId(), request.name(), request.targetExpression(),
                CombineAlgorithmName.valueOf(request.combineAlgorithm())));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    @PutMapping("/{id}")
    @ResourceMapping(resource = "abac_policy", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<Void> update(@PathVariable Long id,
                                        @RequestBody PolicyRequest request) {
        updateHandler.handle(new UpdatePolicy.Command(id, request.targetExpression(),
                CombineAlgorithmName.valueOf(request.combineAlgorithm())));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/delete-preview")
    @ResourceMapping(resource = "abac_policy", action = "READ")
    @PreEnforce
    public ResponseEntity<ApiResponse<GetPolicyDeletePreview.Result>> deletePreview(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(deletePreviewHandler.handle(
                new GetPolicyDeletePreview.Query(id))));
    }

    @DeleteMapping("/{id}")
    @ResourceMapping(resource = "abac_policy", action = "DELETE")
    @PreEnforce
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteHandler.handle(new DeletePolicy.Command(id));
        return ResponseEntity.noContent().build();
    }

    record PolicyRequest(Long policySetId, String name, String targetExpression,
                         String combineAlgorithm) {}
}
