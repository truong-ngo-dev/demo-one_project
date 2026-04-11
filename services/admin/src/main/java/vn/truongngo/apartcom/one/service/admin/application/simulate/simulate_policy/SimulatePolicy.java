package vn.truongngo.apartcom.one.service.admin.application.simulate.simulate_policy;

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
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.AbacPolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.abac.AdminPolicyProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulatePolicy {

    public record SimulateSubjectRequest(
            String userId,
            List<String> roles,
            Map<String, Object> attributes
    ) {}

    public record SimulateResourceRequest(
            String name,
            Object data
    ) {}

    public record Command(
            SimulateSubjectRequest subject,
            SimulateResourceRequest resource,
            String action,
            Long policySetId
    ) {}

    public record SimulateResult(
            String decision,
            long timestamp,
            Long policySetId,
            String policySetName,
            Object details,
            List<RuleTraceEntry> trace
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, SimulateResult> {

        private final PolicySetRepository policySetRepository;
        private final ResourceDefinitionRepository resourceRepository;
        private final PdpEngine pdpEngine;
        private final AdminPolicyProvider adminPolicyProvider;

        @Override
        public SimulateResult handle(Command command) {
            // 1. Validate resource + action exist
            ResourceDefinition resource = resourceRepository.findByName(command.resource().name())
                    .orElseThrow(AbacException::resourceNotFound);
            boolean actionExists = resource.getActions().stream()
                    .anyMatch(a -> a.getName().equalsIgnoreCase(command.action()));
            if (!actionExists) {
                throw AbacException.actionNotFound();
            }

            // 2. Load PolicySet
            PolicySetDefinition policySetDef;
            if (command.policySetId() != null) {
                policySetDef = policySetRepository.findById(PolicySetId.of(command.policySetId()))
                        .orElseThrow(AbacPolicyException::policySetNotFound);
            } else {
                List<PolicySetDefinition> roots = policySetRepository.findAllRoot();
                policySetDef = roots.isEmpty() ? null : roots.get(0);
            }

            AbstractPolicy policy = adminPolicyProvider.getPolicy("admin-service");

            // 3. Build virtual Subject
            Subject subject = new Subject();
            subject.setUserId(command.subject().userId());
            subject.setRoles(command.subject().roles() != null ? command.subject().roles() : List.of());
            subject.setAttributes(command.subject().attributes() != null
                    ? new HashMap<>(command.subject().attributes())
                    : new HashMap<>());

            // 4. Build Action, Resource, Environment
            Action action = Action.semantic(command.action());
            Resource resourceCtx = new Resource(command.resource().name(), command.resource().data());
            Environment environment = new Environment();

            // 5. Authorize with trace
            AuthzRequest authzRequest = new AuthzRequest(subject, resourceCtx, action, environment, policy);
            AuthzDecision authzDecision = pdpEngine.authorizeWithTrace(authzRequest);

            List<RuleTraceEntry> trace = List.of();
            Object details = authzDecision.getDetails();
            if (details instanceof EvaluationDetails evalDetails) {
                trace = evalDetails.trace();
                details = evalDetails.cause();
            }

            return new SimulateResult(
                    authzDecision.isPermit() ? "PERMIT" : "DENY",
                    authzDecision.getTimestamp(),
                    policySetDef != null ? policySetDef.getId().getValue() : null,
                    policySetDef != null ? policySetDef.getName() : null,
                    details,
                    trace
            );
        }
    }
}
