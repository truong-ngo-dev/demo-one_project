package vn.truongngo.apartcom.one.service.admin.domain.role;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.Auditable;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;

@Getter
public class Role extends AbstractAggregateRoot<RoleId> implements AggregateRoot<RoleId> {

    private final String name;
    private final String description;
    private final Auditable auditable;

    private Role(RoleId id, String name, String description, Auditable auditable) {
        super(id);
        this.name        = name;
        this.description = description;
        this.auditable   = auditable;
    }

    public static Role register(String name, String description) {
        Assert.hasText(name, "name is required");
        long now = System.currentTimeMillis();
        return new Role(RoleId.generate(), name, description, new Auditable(now, now, null, null));
    }

    public static Role reconstitute(RoleId id, String name, String description, Auditable auditable) {
        return new Role(id, name, description, auditable);
    }

    public Role updateDescription(String description) {
        long now = System.currentTimeMillis();
        Auditable updated = new Auditable(
                this.auditable.getCreatedAt(), now,
                this.auditable.getCreatedBy(), this.auditable.getUpdatedBy());
        return reconstitute(this.getId(), this.name, description, updated);
    }
}
