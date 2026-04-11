package vn.truongngo.apartcom.one.service.admin.domain.abac.resource;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class AbacException extends DomainException {

    public AbacException(AbacErrorCode errorCode) {
        super(errorCode);
    }

    public static AbacException resourceNotFound() {
        return new AbacException(AbacErrorCode.RESOURCE_NOT_FOUND);
    }

    public static AbacException resourceNameDuplicate() {
        return new AbacException(AbacErrorCode.RESOURCE_NAME_DUPLICATE);
    }

    public static AbacException resourceInUse() {
        return new AbacException(AbacErrorCode.RESOURCE_IN_USE);
    }

    public static AbacException actionNotFound() {
        return new AbacException(AbacErrorCode.ACTION_NOT_FOUND);
    }

    public static AbacException actionNameDuplicate() {
        return new AbacException(AbacErrorCode.ACTION_NAME_DUPLICATE);
    }

    public static AbacException actionInUse() {
        return new AbacException(AbacErrorCode.ACTION_IN_USE);
    }

    public static AbacException uiElementNotFound() {
        return new AbacException(AbacErrorCode.UI_ELEMENT_NOT_FOUND);
    }

    public static AbacException uiElementIdDuplicate() {
        return new AbacException(AbacErrorCode.UI_ELEMENT_ID_DUPLICATE);
    }
}
