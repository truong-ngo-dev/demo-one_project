package vn.truongngo.apartcom.one.service.oauth2.domain.activity;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum LoginActivityErrorCode implements ErrorCode {

    ACTIVITY_NOT_FOUND("03001", "Activity not found", "error.activity.not_found", 404);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    LoginActivityErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    @Override public String code() { return code; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public String messageKey() { return messageKey; }
    @Override public int httpStatus() { return httpStatus; }
}
