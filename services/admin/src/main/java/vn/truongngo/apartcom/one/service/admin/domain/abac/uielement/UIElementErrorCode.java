package vn.truongngo.apartcom.one.service.admin.domain.abac.uielement;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum UIElementErrorCode implements ErrorCode {

    UI_ELEMENT_NOT_FOUND("30012", "UI element not found", "error.abac.ui_element.not_found", 404),
    UI_ELEMENT_ID_DUPLICATE("30013", "UI element ID already exists", "error.abac.ui_element.id_duplicate", 409);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    UIElementErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
        this.code           = code;
        this.defaultMessage = defaultMessage;
        this.messageKey     = messageKey;
        this.httpStatus     = httpStatus;
    }

    @Override public String code()           { return code; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public String messageKey()     { return messageKey; }
    @Override public int httpStatus()        { return httpStatus; }
}
