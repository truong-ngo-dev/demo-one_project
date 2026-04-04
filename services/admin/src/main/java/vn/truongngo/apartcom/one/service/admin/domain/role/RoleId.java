package vn.truongngo.apartcom.one.service.admin.domain.role;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

import java.util.UUID;

public class RoleId extends AbstractId<String> implements Id<String> {

    private RoleId(String value) {
        super(value);
    }

    public static RoleId of(String value) {
        return new RoleId(value);
    }

    public static RoleId generate() {
        return new RoleId(UUID.randomUUID().toString());
    }
}
