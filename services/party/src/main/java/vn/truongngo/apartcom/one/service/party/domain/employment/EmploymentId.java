package vn.truongngo.apartcom.one.service.party.domain.employment;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

import java.util.UUID;

public class EmploymentId extends AbstractId<String> implements Id<String> {

    private EmploymentId(String value) {
        super(value);
    }

    public static EmploymentId of(String value) {
        return new EmploymentId(value);
    }

    public static EmploymentId generate() {
        return new EmploymentId(UUID.randomUUID().toString());
    }
}
