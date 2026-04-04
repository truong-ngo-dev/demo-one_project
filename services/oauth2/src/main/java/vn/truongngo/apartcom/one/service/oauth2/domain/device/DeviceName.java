package vn.truongngo.apartcom.one.service.oauth2.domain.device;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.ValueObject;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;

@Getter
public class DeviceName implements ValueObject {

    private final String value;
    private final DeviceType type;

    private DeviceName(String value, DeviceType type) {
        this.value = value;
        this.type = type;
    }

    public static DeviceName of(String value, DeviceType type) {
        Assert.hasText(value, "DeviceName must not be empty");
        return new DeviceName(value, type);
    }

    public static DeviceName unknown() {
        return new DeviceName("Unknown Device", DeviceType.OTHER);
    }
}
