package vn.truongngo.apartcom.one.service.admin.domain.abac.uielement;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UIElementId {

    private final Long value;

    private UIElementId(Long value) {
        this.value = value;
    }

    public static UIElementId of(Long value) {
        return new UIElementId(value);
    }
}
