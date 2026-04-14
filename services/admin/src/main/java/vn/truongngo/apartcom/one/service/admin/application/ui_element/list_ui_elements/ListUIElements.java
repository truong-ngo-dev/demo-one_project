package vn.truongngo.apartcom.one.service.admin.application.ui_element.list_ui_elements;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.application.rule.service.SpelExpressionAnalyzer;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElement;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListUIElements {

    public record Query(Long resourceId, String type, String group, String scope, int page, int size) {
        public static Query of(Long resourceId, String type, String group, String scope, Integer page, Integer size) {
            return new Query(
                    resourceId, type, group, scope,
                    page != null ? page : 0,
                    size != null ? size : 20
            );
        }
    }

    public record UIElementSummary(
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
    public static class Handler implements QueryHandler<Query, Page<UIElementSummary>> {

        private final UIElementRepository uiElementRepository;
        private final ResourceDefinitionRepository resourceRepository;
        private final PolicySetRepository policySetRepository;
        private final PolicyRepository policyRepository;

        @Override
        public Page<UIElementSummary> handle(Query query) {
            Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by("orderIndex").ascending());
            Scope scopeFilter = query.scope() != null ? Scope.valueOf(query.scope()) : null;
            Page<UIElement> page = uiElementRepository.findAll(query.resourceId(), null, query.group(), scopeFilter, pageable);
            CoverageIndex coverageIndex = buildCoverageIndex();
            // Cache resource lookups to avoid N+1
            Map<Long, ResourceDefinition> resourceCache = new HashMap<>();
            return page.map(e -> {
                Long rid = e.getResourceId().getValue();
                ResourceDefinition resource = resourceCache.computeIfAbsent(rid, id ->
                        resourceRepository.findById(e.getResourceId())
                                .orElseThrow(ResourceException::resourceNotFound));
                String actionName = resource.getActions().stream()
                        .filter(a -> e.getActionId().equals(a.getId()))
                        .findFirst()
                        .map(a -> a.getName())
                        .orElse(null);
                boolean covered = actionName != null &&
                        (coverageIndex.hasWildcard() || coverageIndex.coveredActions().contains(actionName));
                return new UIElementSummary(
                        e.getId().getValue(), e.getElementId(), e.getLabel(),
                        e.getType().name(), e.getScope().name(), e.getElementGroup(), e.getOrderIndex(),
                        rid, resource.getName(),
                        e.getActionId().getValue(), actionName, covered
                );
            });
        }

        private record CoverageIndex(boolean hasWildcard, Set<String> coveredActions) {}

        private CoverageIndex buildCoverageIndex() {
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
            return new CoverageIndex(hasWildcard, coveredActions);
        }
    }
}
