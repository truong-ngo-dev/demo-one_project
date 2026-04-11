package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class PolicyId {

    private final Long value;

    private PolicyId(Long value) {
        this.value = value;
    }

    public static PolicyId of(Long value) {
        return new PolicyId(value);
    }
}
