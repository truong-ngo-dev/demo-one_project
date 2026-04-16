package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root for a Policy containing an ordered list of Rules.
 * name is immutable after creation.
 */
@Getter
public class PolicyDefinition {

    private final PolicyId id;
    private final PolicySetId policySetId;
    private final String name;
    private final ExpressionNode targetExpression;
    private final CombineAlgorithmName combineAlgorithm;
    private final List<RuleDefinition> rules;
    private final long createdAt;
    private final long updatedAt;

    private PolicyDefinition(PolicyId id, PolicySetId policySetId, String name,
                              ExpressionNode targetExpression, CombineAlgorithmName combineAlgorithm,
                              List<RuleDefinition> rules, long createdAt, long updatedAt) {
        this.id               = id;
        this.policySetId      = policySetId;
        this.name             = name;
        this.targetExpression = targetExpression;
        this.combineAlgorithm = combineAlgorithm;
        this.rules            = new ArrayList<>(rules);
        this.createdAt        = createdAt;
        this.updatedAt        = updatedAt;
    }

    public static PolicyDefinition create(PolicySetId policySetId, String name,
                                           ExpressionNode targetExpression,
                                           CombineAlgorithmName combineAlgorithm) {
        Assert.hasText(name, "name is required");
        Assert.notNull(policySetId, "policySetId is required");
        Assert.notNull(combineAlgorithm, "combineAlgorithm is required");
        long now = System.currentTimeMillis();
        return new PolicyDefinition(null, policySetId, name, targetExpression, combineAlgorithm,
                List.of(), now, now);
    }

    public static PolicyDefinition reconstitute(PolicyId id, PolicySetId policySetId, String name,
                                                 ExpressionNode targetExpression,
                                                 CombineAlgorithmName combineAlgorithm,
                                                 List<RuleDefinition> rules,
                                                 long createdAt, long updatedAt) {
        return new PolicyDefinition(id, policySetId, name, targetExpression, combineAlgorithm,
                rules, createdAt, updatedAt);
    }

    public PolicyDefinition updatePolicy(ExpressionNode targetExpression,
                                          CombineAlgorithmName combineAlgorithm) {
        Assert.notNull(combineAlgorithm, "combineAlgorithm is required");
        return new PolicyDefinition(this.id, this.policySetId, this.name, targetExpression,
                combineAlgorithm, this.rules, this.createdAt, System.currentTimeMillis());
    }

    public PolicyDefinition addRule(RuleDefinition rule) {
        List<RuleDefinition> updated = new ArrayList<>(rules);
        updated.add(rule);
        return new PolicyDefinition(this.id, this.policySetId, this.name, this.targetExpression,
                this.combineAlgorithm, updated, this.createdAt, System.currentTimeMillis());
    }

    public PolicyDefinition updateRule(RuleId ruleId, String name, String description,
                                        ExpressionNode targetExpression, ExpressionNode conditionExpression,
                                        Effect effect) {
        boolean found = rules.stream().anyMatch(r -> ruleId.equals(r.getId()));
        if (!found) throw PolicyException.ruleNotFound();
        List<RuleDefinition> updated = rules.stream()
                .map(r -> ruleId.equals(r.getId()) ? r.update(name, description, targetExpression, conditionExpression, effect) : r)
                .toList();
        return new PolicyDefinition(this.id, this.policySetId, this.name, this.targetExpression,
                this.combineAlgorithm, updated, this.createdAt, System.currentTimeMillis());
    }

    public PolicyDefinition removeRule(RuleId ruleId) {
        boolean found = rules.stream().anyMatch(r -> ruleId.equals(r.getId()));
        if (!found) throw PolicyException.ruleNotFound();
        List<RuleDefinition> updated = rules.stream()
                .filter(r -> !ruleId.equals(r.getId()))
                .toList();
        return new PolicyDefinition(this.id, this.policySetId, this.name, this.targetExpression,
                this.combineAlgorithm, updated, this.createdAt, System.currentTimeMillis());
    }

    /**
     * Reorder rules according to the given ordered list of rule IDs.
     * All provided IDs must belong to this policy.
     */
    public PolicyDefinition reorderRules(List<Long> orderedRuleIds) {
        List<RuleDefinition> reordered = new ArrayList<>();
        for (int i = 0; i < orderedRuleIds.size(); i++) {
            RuleId rid = RuleId.of(orderedRuleIds.get(i));
            RuleDefinition rule = rules.stream()
                    .filter(r -> rid.equals(r.getId()))
                    .findFirst()
                    .orElseThrow(PolicyException::ruleNotFound);
            reordered.add(rule.withOrderIndex(i));
        }
        return new PolicyDefinition(this.id, this.policySetId, this.name, this.targetExpression,
                this.combineAlgorithm, reordered, this.createdAt, System.currentTimeMillis());
    }

    public List<RuleDefinition> getRules() {
        return Collections.unmodifiableList(rules);
    }
}
