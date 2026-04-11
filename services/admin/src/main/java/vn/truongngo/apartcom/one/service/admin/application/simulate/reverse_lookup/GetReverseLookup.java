package vn.truongngo.apartcom.one.service.admin.application.simulate.reverse_lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.application.rule.SpelExpressionAnalyzer;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class GetReverseLookup {

    public record Query(
            String resourceName,
            String actionName,
            Long policySetId    // null → use root policy set
    ) {}

    public record RuleCoverage(
            Long ruleId,
            String ruleName,
            String policyName,
            String effect,
            List<String> requiredRoles,
            List<String> requiredAttributes,
            boolean hasInstanceCondition,
            Long userCountByRole,
            String userCountNote
    ) {}

    public record Result(
            String resourceName,
            String actionName,
            List<RuleCoverage> permitRules,
            List<RuleCoverage> denyRules
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final ResourceDefinitionRepository resourceRepository;
        private final PolicySetRepository policySetRepository;
        private final PolicyRepository policyRepository;
        private final UserRepository userRepository;

        @Override
        public Result handle(Query query) {
            // 1. Validate resource + action
            ResourceDefinition resource = resourceRepository.findByName(query.resourceName())
                    .orElseThrow(AbacException::resourceNotFound);
            boolean actionExists = resource.getActions().stream()
                    .anyMatch(a -> a.getName().equalsIgnoreCase(query.actionName()));
            if (!actionExists) {
                throw AbacException.actionNotFound();
            }

            // 2. Load policy set
            PolicySetDefinition policySetDef;
            if (query.policySetId() != null) {
                policySetDef = policySetRepository.findById(PolicySetId.of(query.policySetId()))
                        .orElse(null);
            } else {
                List<PolicySetDefinition> roots = policySetRepository.findAllRoot();
                policySetDef = roots.isEmpty() ? null : roots.get(0);
            }

            // 3. Collect all rules from the policy set
            List<RuleCoverage> permitRules = new ArrayList<>();
            List<RuleCoverage> denyRules   = new ArrayList<>();

            if (policySetDef != null) {
                List<PolicyDefinition> policies = policyRepository.findByPolicySetId(policySetDef.getId());
                for (PolicyDefinition policy : policies) {
                    for (RuleDefinition rule : policy.getRules()) {
                        RuleCoverage coverage = analyzeCoverage(rule, policy.getName(), query.actionName());
                        if (coverage == null) continue;
                        if (rule.getEffect() == Effect.PERMIT) {
                            permitRules.add(coverage);
                        } else {
                            denyRules.add(coverage);
                        }
                    }
                }
            }

            return new Result(query.resourceName(), query.actionName(), permitRules, denyRules);
        }

        /**
         * Returns a {@link RuleCoverage} for the rule if it covers the given actionName,
         * or {@code null} if the rule's target constraints exclude this action.
         */
        private RuleCoverage analyzeCoverage(RuleDefinition rule, String policyName, String actionName) {
            String targetExpr    = rule.getTargetExpression()    != null ? rule.getTargetExpression().spElExpression()    : null;
            String conditionExpr = rule.getConditionExpression() != null ? rule.getConditionExpression().spElExpression() : null;

            SpelExpressionAnalyzer.AnalysisResult analysis =
                    SpelExpressionAnalyzer.analyze(targetExpr, conditionExpr);

            // If the analysis has specific actions and our actionName is not among them → skip
            if (!analysis.specificActions().isEmpty()
                    && analysis.specificActions().stream().noneMatch(a -> a.equalsIgnoreCase(actionName))) {
                return null;
            }

            // Compute user count
            Long userCountByRole = null;
            String userCountNote = null;

            if (analysis.requiredRoles().isEmpty()) {
                userCountNote = "Applies to all users";
            } else if (analysis.requiredRoles().size() == 1) {
                userCountByRole = userRepository.countByRoleName(analysis.requiredRoles().get(0));
            } else {
                long sum = analysis.requiredRoles().stream()
                        .mapToLong(roleName -> userRepository.countByRoleName(roleName))
                        .sum();
                userCountByRole = sum;
                userCountNote = "Sum across all roles (may overlap)";
            }

            return new RuleCoverage(
                    rule.getId() != null ? rule.getId().getValue() : null,
                    rule.getName(),
                    policyName,
                    rule.getEffect().name(),
                    analysis.requiredRoles(),
                    analysis.requiredAttributes(),
                    analysis.hasInstanceCondition(),
                    userCountByRole,
                    userCountNote
            );
        }
    }
}
