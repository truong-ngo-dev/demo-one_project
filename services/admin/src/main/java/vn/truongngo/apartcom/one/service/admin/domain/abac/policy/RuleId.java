package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class RuleId {

    private final Long value;

    private RuleId(Long value) {
        this.value = value;
    }

    public static RuleId of(Long value) {
        return new RuleId(value);
    }
}
