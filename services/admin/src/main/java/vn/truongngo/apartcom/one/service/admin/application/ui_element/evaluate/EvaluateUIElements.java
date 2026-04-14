package vn.truongngo.apartcom.one.service.admin.application.ui_element.evaluate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.abac.context.Action;
import vn.truongngo.apartcom.one.lib.abac.context.Environment;
import vn.truongngo.apartcom.one.lib.abac.context.Resource;
import vn.truongngo.apartcom.one.lib.abac.context.Subject;
import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;
import vn.truongngo.apartcom.one.lib.abac.pdp.AuthzDecision;
import vn.truongngo.apartcom.one.lib.abac.pdp.AuthzRequest;
import vn.truongngo.apartcom.one.lib.abac.pdp.PdpEngine;
import vn.truongngo.apartcom.one.lib.abac.pip.PolicyProvider;
import vn.truongngo.apartcom.one.lib.abac.pip.SubjectProvider;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElement;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementRepository;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluateUIElements {

    public record Query(List<String> elementIds, Principal principal) {}

    public record Result(Map<String, String> results) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final UIElementRepository uiElementRepository;
        private final ResourceDefinitionRepository resourceRepository;
        private final PdpEngine pdpEngine;
        private final PolicyProvider policyProvider;
        private final SubjectProvider subjectProvider;

        @Override
        public Result handle(Query query) {
            List<UIElement> elements = uiElementRepository.findByElementIds(query.elementIds());

            Subject subject = subjectProvider.getSubject(query.principal());

            // Load policy once and reuse for all elements
            AbstractPolicy policy = policyProvider.getPolicy("admin-service");

            // Cache resource lookups
            Map<Long, ResourceDefinition> resourceCache = new HashMap<>();
            Map<String, String> results = new HashMap<>();

            for (UIElement element : elements) {
                Long rid = element.getResourceId().getValue();
                ResourceDefinition resource = resourceCache.computeIfAbsent(rid,
                        id -> resourceRepository.findById(element.getResourceId()).orElse(null));

                String resourceName = resource != null ? resource.getName() : "unknown";
                String actionName = resource != null
                        ? resource.getActions().stream()
                                .filter(a -> element.getActionId().equals(a.getId()))
                                .findFirst()
                                .map(ActionDefinition::getName)
                                .orElse("unknown")
                        : "unknown";

                Action action = Action.semantic(actionName);
                Resource resourceCtx = new Resource(resourceName, null);
                Environment environment = new Environment();

                AuthzRequest request = new AuthzRequest(subject, resourceCtx, action, environment, policy);
                AuthzDecision decision = pdpEngine.authorize(request);
                results.put(element.getElementId(), decision.isPermit() ? "PERMIT" : "DENY");
            }

            // Elements not found in DB → DENY
            for (String elementId : query.elementIds()) {
                results.putIfAbsent(elementId, "DENY");
            }

            return new Result(results);
        }
    }
}
