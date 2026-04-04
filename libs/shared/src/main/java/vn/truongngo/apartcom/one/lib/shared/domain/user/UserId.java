package vn.truongngo.apartcom.one.lib.shared.domain.user;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

public class UserId extends AbstractId<String> implements Id<String> {

    public UserId(String value) {
        super(value);
    }
}
