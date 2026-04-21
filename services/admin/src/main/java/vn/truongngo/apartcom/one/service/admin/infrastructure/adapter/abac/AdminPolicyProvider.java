package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.abac;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;
import vn.truongngo.apartcom.one.lib.abac.domain.Expression;
import vn.truongngo.apartcom.one.lib.abac.domain.Policy;
import vn.truongngo.apartcom.one.lib.abac.domain.PolicySet;
import vn.truongngo.apartcom.one.lib.abac.domain.Rule;
import vn.truongngo.apartcom.one.lib.abac.pip.PolicyProvider;
import vn.truongngo.apartcom.one.service.admin.application.expression.ExpressionTreeService;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionNode;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;

import java.util.List;

/**
 * Loads the root PolicySet from the DB and maps it to the libs/abac domain model
 * for evaluation by PdpEngine.
 * Phase 1: returns the single root PolicySet, ignoring serviceName.
 */
@Component
@RequiredArgsConstructor
public class AdminPolicyProvider implements PolicyProvider {

    private final PolicySetRepository policySetRepository;
    private final PolicyRepository policyRepository;
    private final ExpressionTreeService expressionTreeService;
    private final HttpServletRequest request;

    @Override
    public AbstractPolicy getPolicy(String serviceName) {
        String scopeHeader = request.getHeader("X-Portal-Scope");
        Scope scope = null;

        if (StringUtils.hasText(scopeHeader)) {
            try {
                scope = Scope.valueOf(scopeHeader);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (scope == null) {
            return emptyPolicySet("No X-Portal-Scope header or unknown scope");
        }

        List<PolicySetDefinition> roots = policySetRepository.findAllRoot();
        final Scope finalScope = scope;
        PolicySetDefinition rootPs = roots.stream()
                .filter(pd -> pd.getScope().equals(finalScope))
                .findFirst()
                .orElse(null);

        if (rootPs == null) {
            return emptyPolicySet("No root policy set configured for scope: " + scope);
        }

        return mapPolicySet(rootPs);
    }

    private PolicySet emptyPolicySet(String description) {
        PolicySet empty = new PolicySet();
        empty.setId("system-root");
        empty.setDescription(description);
        empty.setCombineAlgorithmName(vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName.DENY_OVERRIDES);
        empty.setIsRoot(true);
        empty.setPolicies(List.of());
        return empty;
    }

    private PolicySet mapPolicySet(PolicySetDefinition ps) {
        List<PolicyDefinition> policies = policyRepository.findByPolicySetId(ps.getId());
        List<AbstractPolicy> abacPolicies = policies.stream()
                .map(p -> (AbstractPolicy) mapPolicy(p))
                .toList();

        PolicySet abacPs = new PolicySet();
        abacPs.setId(String.valueOf(ps.getId().getValue()));
        abacPs.setDescription(ps.getName());
        abacPs.setTarget(null);
        abacPs.setCombineAlgorithmName(ps.getCombineAlgorithm());
        abacPs.setIsRoot(ps.isRoot());
        abacPs.setPolicies(abacPolicies);
        return abacPs;
    }

    private Policy mapPolicy(PolicyDefinition pd) {
        List<Rule> rules = pd.getRules().stream()
                .map(this::mapRule)
                .toList();

        Policy abacPolicy = new Policy();
        abacPolicy.setId(String.valueOf(pd.getId().getValue()));
        abacPolicy.setDescription(pd.getName());
        abacPolicy.setTarget(toAbacExpression(pd.getTargetExpression()));
        abacPolicy.setCombineAlgorithmName(pd.getCombineAlgorithm());
        abacPolicy.setIsRoot(false);
        abacPolicy.setRules(rules);
        return abacPolicy;
    }

    private Rule mapRule(RuleDefinition rd) {
        Rule rule = new Rule();
        rule.setId(String.valueOf(rd.getId().getValue()));
        rule.setDescription(rd.getName());
        rule.setTarget(toAbacExpression(rd.getTargetExpression()));
        rule.setCondition(toAbacExpression(rd.getConditionExpression()));
        rule.setEffect(rd.getEffect() == vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect.PERMIT
                ? Rule.Effect.PERMIT : Rule.Effect.DENY);
        return rule;
    }

    private Expression toAbacExpression(ExpressionNode node) {
        if (node == null) return null;
        String spel = expressionTreeService.resolveFromNode(node);
        Expression expr = new Expression();
        expr.setType(Expression.Type.LITERAL);
        expr.setExpression(spel);
        expr.setSubExpressions(null);
        expr.setCombinationType(null);
        return expr;
    }
}
