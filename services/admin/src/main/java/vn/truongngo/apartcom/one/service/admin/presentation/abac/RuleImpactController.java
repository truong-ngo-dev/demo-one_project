package vn.truongngo.apartcom.one.service.admin.presentation.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.truongngo.apartcom.one.service.admin.application.rule.query.impact_preview.GetRuleImpactPreview;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.ImpactPreviewRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;

@RestController
@RequestMapping("/api/v1/abac/rules")
@RequiredArgsConstructor
public class RuleImpactController {

    private final GetRuleImpactPreview.Handler impactHandler;

    @PostMapping("/impact-preview")
    public ResponseEntity<ApiResponse<GetRuleImpactPreview.Result>> impactPreview(
            @RequestBody ImpactPreviewRequest request) {
        GetRuleImpactPreview.Query query = new GetRuleImpactPreview.Query(
                request.targetExpression(),
                request.conditionExpression());
        return ResponseEntity.ok(ApiResponse.of(impactHandler.handle(query)));
    }
}
