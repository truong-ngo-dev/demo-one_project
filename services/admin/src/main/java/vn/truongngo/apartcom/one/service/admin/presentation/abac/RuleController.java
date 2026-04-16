package vn.truongngo.apartcom.one.service.admin.presentation.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.lib.abac.pep.PreEnforce;
import vn.truongngo.apartcom.one.lib.abac.rap.ResourceMapping;
import vn.truongngo.apartcom.one.service.admin.application.rule.create_rule.CreateRule;
import vn.truongngo.apartcom.one.service.admin.application.rule.delete_rule.DeleteRule;
import vn.truongngo.apartcom.one.service.admin.application.rule.reorder_rules.ReorderRules;
import vn.truongngo.apartcom.one.service.admin.application.rule.update_rule.UpdateRule;
import vn.truongngo.apartcom.one.service.admin.application.rule.get_rule.GetRule;
import vn.truongngo.apartcom.one.service.admin.application.rule.list_rules.ListRules;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionNode;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.ExpressionNodeRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/abac/policies/{policyId}/rules")
@RequiredArgsConstructor
public class RuleController {

    private final CreateRule.Handler createHandler;
    private final UpdateRule.Handler updateHandler;
    private final DeleteRule.Handler deleteHandler;
    private final ReorderRules.Handler reorderHandler;
    private final GetRule.Handler getHandler;
    private final ListRules.Handler listHandler;

    @GetMapping
    @ResourceMapping(resource = "abac_rule", action = "LIST")
    @PreEnforce
    public ResponseEntity<ApiResponse<List<GetRule.Result>>> list(@PathVariable Long policyId) {
        return ResponseEntity.ok(ApiResponse.of(listHandler.handle(new ListRules.Query(policyId))));
    }

    @GetMapping("/{ruleId}")
    @ResourceMapping(resource = "abac_rule", action = "READ")
    @PreEnforce
    public ResponseEntity<ApiResponse<GetRule.Result>> get(
            @PathVariable Long policyId, @PathVariable Long ruleId) {
        return ResponseEntity.ok(ApiResponse.of(getHandler.handle(new GetRule.Query(policyId, ruleId))));
    }

    @PostMapping
    @ResourceMapping(resource = "abac_rule", action = "CREATE")
    @PreEnforce
    public ResponseEntity<ApiResponse<CreateRule.Result>> create(
            @PathVariable Long policyId, @RequestBody RuleRequest request) {
        CreateRule.Result result = createHandler.handle(new CreateRule.Command(
                policyId, request.name(), request.description(),
                toExpressionNode(request.targetExpression()),
                toExpressionNode(request.conditionExpression()),
                Effect.valueOf(request.effect()), request.orderIndex()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    @PutMapping("/{ruleId}")
    @ResourceMapping(resource = "abac_rule", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<Void> update(
            @PathVariable Long policyId, @PathVariable Long ruleId,
            @RequestBody RuleRequest request) {
        updateHandler.handle(new UpdateRule.Command(
                policyId, ruleId, request.name(), request.description(),
                toExpressionNode(request.targetExpression()),
                toExpressionNode(request.conditionExpression()),
                Effect.valueOf(request.effect())));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{ruleId}")
    @ResourceMapping(resource = "abac_rule", action = "DELETE")
    @PreEnforce
    public ResponseEntity<Void> delete(
            @PathVariable Long policyId, @PathVariable Long ruleId) {
        deleteHandler.handle(new DeleteRule.Command(policyId, ruleId));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    @ResourceMapping(resource = "abac_rule", action = "UPDATE")
    @PreEnforce
    public ResponseEntity<Void> reorder(
            @PathVariable Long policyId, @RequestBody ReorderRequest request) {
        reorderHandler.handle(new ReorderRules.Command(policyId, request.ruleIds()));
        return ResponseEntity.noContent().build();
    }

    private ExpressionNode toExpressionNode(ExpressionNodeRequest req) {
        if (req == null) return null;
        return req.toDomain();
    }

    record RuleRequest(String name, String description,
                       ExpressionNodeRequest targetExpression,
                       ExpressionNodeRequest conditionExpression,
                       String effect, Integer orderIndex) {}

    record ReorderRequest(List<Long> ruleIds) {}
}
