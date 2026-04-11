package vn.truongngo.apartcom.one.service.admin.application.ui_element.query.list_uncovered_ui_elements;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.application.rule.SpelExpressionAnalyzer;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElement;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListUncoveredUIElements {

    public record Query() {}

    public record UncoveredUIElement(
            Long id,
            String elementId,
            String label,
            String type,
            String elementGroup,
            Long resourceId,
            String resourceName,
            Long actionId,
            String actionName
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<UncoveredUIElement>> {

        private final UIElementRepository uiElementRepository;
        private final ResourceDefinitionRepository resourceRepository;
        private final PolicySetRepository policySetRepository;
        private final PolicyRepository policyRepository;

        @Override
        public List<UncoveredUIElement> handle(Query query) {
            // Build coverage index from PERMIT rules
            Set<String> coveredActions = new HashSet<>();
            boolean hasWildcard = false;
            List<PolicySetDefinition> roots = policySetRepository.findAllRoot();
            outer:
            for (PolicySetDefinition policySet : roots) {
                List<PolicyDefinition> policies = policyRepository.findByPolicySetId(policySet.getId());
                for (PolicyDefinition policy : policies) {
                    for (RuleDefinition rule : policy.getRules()) {
                        if (rule.getEffect() != Effect.PERMIT) continue;
                        String target = rule.getTargetExpression() != null
                                ? rule.getTargetExpression().spElExpression() : null;
                        String condition = rule.getConditionExpression() != null
                                ? rule.getConditionExpression().spElExpression() : null;
                        SpelExpressionAnalyzer.AnalysisResult result =
                                SpelExpressionAnalyzer.analyze(target, condition);
                        if (result.specificActions().isEmpty()) {
                            hasWildcard = true;
                            break outer;
                        }
                        coveredActions.addAll(result.specificActions());
                    }
                }
            }

            // If wildcard coverage exists, all elements are covered
            if (hasWildcard) {
                return List.of();
            }

            // Load all UIElements and filter uncovered ones
            var pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("orderIndex").ascending());
            List<UIElement> all = uiElementRepository.findAll(null, null, null, pageable).getContent();

            Map<Long, ResourceDefinition> resourceCache = new HashMap<>();
            List<UncoveredUIElement> uncovered = new ArrayList<>();
            for (UIElement element : all) {
                Long rid = element.getResourceId().getValue();
                ResourceDefinition resource = resourceCache.computeIfAbsent(rid, id ->
                        resourceRepository.findById(element.getResourceId())
                                .orElseThrow(AbacException::resourceNotFound));
                String actionName = resource.getActions().stream()
                        .filter(a -> element.getActionId().equals(a.getId()))
                        .findFirst()
                        .map(a -> a.getName())
                        .orElse(null);
                if (actionName == null || !coveredActions.contains(actionName)) {
                    uncovered.add(new UncoveredUIElement(
                            element.getId(), element.getElementId(), element.getLabel(),
                            element.getType().name(), element.getElementGroup(),
                            rid, resource.getName(),
                            element.getActionId().getValue(), actionName
                    ));
                }
            }
            return uncovered;
        }
    }
}
