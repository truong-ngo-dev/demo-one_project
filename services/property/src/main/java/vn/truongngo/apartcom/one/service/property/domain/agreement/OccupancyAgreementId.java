package vn.truongngo.apartcom.one.service.property.domain.agreement;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

import java.util.UUID;

public class OccupancyAgreementId extends AbstractId<String> implements Id<String> {

    private OccupancyAgreementId(String value) {
        super(value);
    }

    public static OccupancyAgreementId of(String value) {
        return new OccupancyAgreementId(value);
    }

    public static OccupancyAgreementId generate() {
        return new OccupancyAgreementId(UUID.randomUUID().toString());
    }
}
