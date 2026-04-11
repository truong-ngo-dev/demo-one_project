package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import lombok.Getter;

/**
 * Entity representing a Rule within a Policy.
 * Part of the PolicyDefinition aggregate.
 */
@Getter
public class RuleDefinition {

    private final RuleId id;
    private final PolicyId policyId;
    private final String name;
    private final String description;
    private final ExpressionVO targetExpression;
    private final ExpressionVO conditionExpression;
    private final Effect effect;
    private final int orderIndex;
    private final long createdAt;
    private final long updatedAt;

    private RuleDefinition(RuleId id, PolicyId policyId, String name, String description,
                            ExpressionVO targetExpression, ExpressionVO conditionExpression,
                            Effect effect, int orderIndex, long createdAt, long updatedAt) {
        this.id                  = id;
        this.policyId            = policyId;
        this.name                = name;
        this.description         = description;
        this.targetExpression    = targetExpression;
        this.conditionExpression = conditionExpression;
        this.effect              = effect;
        this.orderIndex          = orderIndex;
        this.createdAt           = createdAt;
        this.updatedAt           = updatedAt;
    }

    public static RuleDefinition create(PolicyId policyId, String name, String description,
                                        ExpressionVO targetExpression, ExpressionVO conditionExpression,
                                        Effect effect, int orderIndex) {
        long now = System.currentTimeMillis();
        return new RuleDefinition(null, policyId, name, description,
                targetExpression, conditionExpression, effect, orderIndex, now, now);
    }

    public static RuleDefinition reconstitute(RuleId id, PolicyId policyId, String name,
                                              String description, ExpressionVO targetExpression,
                                              ExpressionVO conditionExpression, Effect effect,
                                              int orderIndex, long createdAt, long updatedAt) {
        return new RuleDefinition(id, policyId, name, description,
                targetExpression, conditionExpression, effect, orderIndex, createdAt, updatedAt);
    }

    public RuleDefinition update(String name, String description, ExpressionVO targetExpression,
                                 ExpressionVO conditionExpression, Effect effect) {
        return new RuleDefinition(this.id, this.policyId, name, description,
                targetExpression, conditionExpression, effect, this.orderIndex,
                this.createdAt, System.currentTimeMillis());
    }

    public RuleDefinition withOrderIndex(int newOrderIndex) {
        return new RuleDefinition(this.id, this.policyId, this.name, this.description,
                this.targetExpression, this.conditionExpression, this.effect,
                newOrderIndex, this.createdAt, System.currentTimeMillis());
    }
}
