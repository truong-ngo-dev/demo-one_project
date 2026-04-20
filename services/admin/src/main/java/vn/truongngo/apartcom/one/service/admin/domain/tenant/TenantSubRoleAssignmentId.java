package vn.truongngo.apartcom.one.service.admin.domain.tenant;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

import java.util.UUID;

public class TenantSubRoleAssignmentId extends AbstractId<String> implements Id<String> {

    private TenantSubRoleAssignmentId(String value) {
        super(value);
    }

    public static TenantSubRoleAssignmentId of(String value) {
        return new TenantSubRoleAssignmentId(value);
    }

    public static TenantSubRoleAssignmentId generate() {
        return new TenantSubRoleAssignmentId(UUID.randomUUID().toString());
    }
}
