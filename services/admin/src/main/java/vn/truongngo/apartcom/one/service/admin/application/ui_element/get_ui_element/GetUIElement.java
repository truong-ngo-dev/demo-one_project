package vn.truongngo.apartcom.one.service.admin.application.ui_element.get_ui_element;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.application.expression.ExpressionTreeService;
import vn.truongngo.apartcom.one.service.admin.application.rule.service.SpelExpressionAnalyzer;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElement;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementRepository;

import java.util.List;

public class GetUIElement {

    public record Query(Long id) {}

    public record UIElementView(
            Long id,
            String elementId,
            String label,
            String type,
            String scope,
            String elementGroup,
            int orderIndex,
            Long resourceId,
            String resourceName,
            Long actionId,
            String actionName,
            boolean hasPolicyCoverage
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, UIElementView> {

        private final UIElementRepository uiElementRepository;
        private final ResourceDefinitionRepository resourceRepository;
        private final PolicySetRepository policySetRepository;
        private final PolicyRepository policyRepository;
        private final ExpressionTreeService expressionTreeService;

        @Override
        public UIElementView handle(Query query) {
            UIElement element = uiElementRepository.findById(UIElementId.of(query.id()))
                    .orElseThrow(UIElementException::uiElementNotFound);
            return toView(element);
        }

        UIElementView toView(UIElement element) {
            ResourceDefinition resource = resourceRepository.findById(element.getResourceId())
                    .orElseThrow(ResourceException::resourceNotFound);
            String actionName = resource.getActions().stream()
                    .filter(a -> element.getActionId().equals(a.getId()))
                    .findFirst()
                    .map(ActionDefinition::getName)
                    .orElse(null);
            boolean covered = actionName != null && computeCoverage(actionName);
            return new UIElementView(
                    element.getId().getValue(),
                    element.getElementId(),
                    element.getLabel(),
                    element.getType().name(),
                    element.getScope().name(),
                    element.getElementGroup(),
                    element.getOrderIndex(),
                    element.getResourceId().getValue(),
                    resource.getName(),
                    element.getActionId().getValue(),
                    actionName,
                    covered
            );
        }

        private boolean computeCoverage(String actionName) {
            List<PolicySetDefinition> roots = policySetRepository.findAllRoot();
            for (PolicySetDefinition policySet : roots) {
                List<PolicyDefinition> policies = policyRepository.findByPolicySetId(policySet.getId());
                for (PolicyDefinition policy : policies) {
                    for (RuleDefinition rule : policy.getRules()) {
                        if (rule.getEffect() != Effect.PERMIT) continue;
                        String target = expressionTreeService.resolveFromNode(rule.getTargetExpression());
                        String condition = expressionTreeService.resolveFromNode(rule.getConditionExpression());
                        SpelExpressionAnalyzer.AnalysisResult result =
                                SpelExpressionAnalyzer.analyze(target, condition);
                        if (result.specificActions().isEmpty()
                                || result.specificActions().contains(actionName)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}
