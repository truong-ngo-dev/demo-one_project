package vn.truongngo.apartcom.one.service.admin.domain.user;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

import java.util.UUID;

public class UserId extends AbstractId<String> implements Id<String> {
    private UserId(String value) { super(value); }

    public static UserId of(String value) { return new UserId(value); }
    public static UserId generate() { return new UserId(UUID.randomUUID().toString()); }
}
