package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class PolicySetId {

    private final Long value;

    private PolicySetId(Long value) {
        this.value = value;
    }

    public static PolicySetId of(Long value) {
        return new PolicySetId(value);
    }
}
