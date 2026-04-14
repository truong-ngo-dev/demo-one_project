package vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;

/**
 * Aggregate Root for a named collection of Policies sharing combine algorithm and scope.
 * name is immutable after creation.
 */
@Getter
public class PolicySetDefinition {

    private final PolicySetId id;
    private final String name;
    private final Scope scope;
    private final CombineAlgorithmName combineAlgorithm;
    private final boolean isRoot;
    private final String tenantId;
    private final long createdAt;
    private final long updatedAt;

    private PolicySetDefinition(PolicySetId id, String name, Scope scope,
                                 CombineAlgorithmName combineAlgorithm, boolean isRoot,
                                 String tenantId, long createdAt, long updatedAt) {
        this.id               = id;
        this.name             = name;
        this.scope            = scope;
        this.combineAlgorithm = combineAlgorithm;
        this.isRoot           = isRoot;
        this.tenantId         = tenantId;
        this.createdAt        = createdAt;
        this.updatedAt        = updatedAt;
    }

    public static PolicySetDefinition create(String name, Scope scope,
                                              CombineAlgorithmName combineAlgorithm,
                                              boolean isRoot, String tenantId) {
        Assert.hasText(name, "name is required");
        Assert.notNull(scope, "scope is required");
        Assert.notNull(combineAlgorithm, "combineAlgorithm is required");
        long now = System.currentTimeMillis();
        return new PolicySetDefinition(null, name, scope, combineAlgorithm, isRoot, tenantId, now, now);
    }

    public static PolicySetDefinition reconstitute(PolicySetId id, String name, Scope scope,
                                                    CombineAlgorithmName combineAlgorithm,
                                                    boolean isRoot, String tenantId,
                                                    long createdAt, long updatedAt) {
        return new PolicySetDefinition(id, name, scope, combineAlgorithm, isRoot, tenantId, createdAt, updatedAt);
    }

    public PolicySetDefinition update(Scope scope, CombineAlgorithmName combineAlgorithm,
                                       boolean isRoot, String tenantId) {
        Assert.notNull(scope, "scope is required");
        Assert.notNull(combineAlgorithm, "combineAlgorithm is required");
        return new PolicySetDefinition(this.id, this.name, scope, combineAlgorithm, isRoot, tenantId,
                this.createdAt, System.currentTimeMillis());
    }
}
