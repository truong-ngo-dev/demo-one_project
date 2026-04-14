package vn.truongngo.apartcom.one.service.admin.domain.abac.uielement;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class UIElementException extends DomainException {

    public UIElementException(UIElementErrorCode errorCode) {
        super(errorCode);
    }

    public static UIElementException uiElementNotFound() {
        return new UIElementException(UIElementErrorCode.UI_ELEMENT_NOT_FOUND);
    }

    public static UIElementException uiElementIdDuplicate() {
        return new UIElementException(UIElementErrorCode.UI_ELEMENT_ID_DUPLICATE);
    }
}
