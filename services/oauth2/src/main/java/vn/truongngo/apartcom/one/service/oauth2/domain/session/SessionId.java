package vn.truongngo.apartcom.one.service.oauth2.domain.session;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

public class SessionId extends AbstractId<String> implements Id<String> {
    public SessionId(String value) {
        super(value);
    }
}
