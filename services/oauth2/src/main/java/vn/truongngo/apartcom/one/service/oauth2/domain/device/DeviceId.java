package vn.truongngo.apartcom.one.service.oauth2.domain.device;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

public class DeviceId extends AbstractId<String> implements Id<String> {

    public DeviceId(String value) {
        super(value);
    }
}
