package vn.truongngo.apartcom.one.service.admin.domain.role;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.Auditable;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;

@Getter
public class Role extends AbstractAggregateRoot<RoleId> implements AggregateRoot<RoleId> {

    private final String name;
    private final String description;
    private final Scope scope;
    private final Auditable auditable;

    private Role(RoleId id, String name, String description, Scope scope, Auditable auditable) {
        super(id);
        this.name        = name;
        this.description = description;
        this.scope       = scope;
        this.auditable   = auditable;
    }

    public static Role register(String name, String description, Scope scope) {
        Assert.hasText(name, "name is required");
        Assert.notNull(scope, "scope is required");
        long now = System.currentTimeMillis();
        return new Role(RoleId.generate(), name, description, scope, new Auditable(now, now, null, null));
    }

    public static Role reconstitute(RoleId id, String name, String description, Scope scope, Auditable auditable) {
        return new Role(id, name, description, scope, auditable);
    }

    public Role updateDescription(String description) {
        long now = System.currentTimeMillis();
        Auditable updated = new Auditable(
                this.auditable.getCreatedAt(), now,
                this.auditable.getCreatedBy(), this.auditable.getUpdatedBy());
        return reconstitute(this.getId(), this.name, description, this.scope, updated);
    }
}
