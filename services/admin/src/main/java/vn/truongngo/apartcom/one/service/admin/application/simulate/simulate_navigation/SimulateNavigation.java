package vn.truongngo.apartcom.one.service.admin.application.simulate.simulate_navigation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.abac.context.Action;
import vn.truongngo.apartcom.one.lib.abac.context.Environment;
import vn.truongngo.apartcom.one.lib.abac.context.Resource;
import vn.truongngo.apartcom.one.lib.abac.context.Subject;
import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;
import vn.truongngo.apartcom.one.lib.abac.evaluation.EvaluationDetails;
import vn.truongngo.apartcom.one.lib.abac.evaluation.RuleTraceEntry;
import vn.truongngo.apartcom.one.lib.abac.pdp.AuthzDecision;
import vn.truongngo.apartcom.one.lib.abac.pdp.AuthzRequest;
import vn.truongngo.apartcom.one.lib.abac.pdp.PdpEngine;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.application.simulate.simulate_policy.SimulatePolicy;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.abac.AdminPolicyProvider;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SimulateNavigation {

    public record Query(
            SimulatePolicy.SimulateSubjectRequest subject,
            String resourceName,
            Long policySetId   // null → use root PolicySet
    ) {}

    public record ActionDecision(String action, String decision, String matchedRuleName) {}

    public record Result(
            String resourceName,
            Long policySetId,
            String policySetName,
            List<ActionDecision> decisions
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final ResourceDefinitionRepository resourceRepository;
        private final PolicySetRepository policySetRepository;
        private final PdpEngine pdpEngine;
        private final AdminPolicyProvider adminPolicyProvider;

        @Override
        public Result handle(Query query) {
            // 1. Resolve resource
            ResourceDefinition resource = resourceRepository.findByName(query.resourceName())
                    .orElseThrow(ResourceException::resourceNotFound);

            // 2. Resolve PolicySet for metadata (id/name in response)
            PolicySetDefinition policySetDef = null;
            if (query.policySetId() != null) {
                policySetDef = policySetRepository.findById(PolicySetId.of(query.policySetId()))
                        .orElseThrow(PolicySetException::policySetNotFound);
            } else {
                List<PolicySetDefinition> roots = policySetRepository.findAllRoot();
                if (!roots.isEmpty()) {
                    policySetDef = roots.get(0);
                }
            }

            // 3. Load policy (always from root — AdminPolicyProvider ignores serviceName in Phase 1)
            AbstractPolicy policy = adminPolicyProvider.getPolicy("admin-service");

            // 4. Build Subject
            Subject subject = new Subject();
            subject.setUserId(query.subject().userId());
            subject.setRoles(query.subject().roles() != null ? query.subject().roles() : List.of());
            subject.setAttributes(query.subject().attributes() != null
                    ? new HashMap<>(query.subject().attributes())
                    : new HashMap<>());

            // 5. Evaluate each action at navigation level (object.data = null)
            List<ActionDecision> decisions = resource.getActions().stream()
                    .sorted(Comparator.comparing(ActionDefinition::getName))
                    .map(actionDef -> {
                        Action action = Action.semantic(actionDef.getName());
                        Resource resourceCtx = new Resource(resource.getName(), null);
                        AuthzRequest request = new AuthzRequest(subject, resourceCtx, action, new Environment(), policy);
                        AuthzDecision decision = pdpEngine.authorizeWithTrace(request);

                        String matchedRuleName = null;
                        if (decision.getDetails() instanceof EvaluationDetails evalDetails) {
                            matchedRuleName = evalDetails.trace().stream()
                                    .filter(RuleTraceEntry::wasDeciding)
                                    .findFirst()
                                    .map(RuleTraceEntry::ruleDescription)
                                    .orElse(null);
                        }

                        return new ActionDecision(
                                actionDef.getName(),
                                decision.isPermit() ? "PERMIT" : "DENY",
                                matchedRuleName
                        );
                    })
                    .toList();

            return new Result(
                    resource.getName(),
                    policySetDef != null ? policySetDef.getId().getValue() : null,
                    policySetDef != null ? policySetDef.getName() : null,
                    decisions
            );
        }
    }
}
