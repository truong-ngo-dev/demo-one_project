package vn.truongngo.apartcom.one.service.admin.domain.user;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("10001", "User not found", "error.user.not_found", 404),
    USER_NOT_ACTIVE("10002", "User is not active", "error.user.not_active", 400),
    USER_NOT_LOCKED("10003", "User is not locked", "error.user.not_locked", 400),
    USER_ALREADY_LOCKED("10004", "User is already locked", "error.user.already_locked", 400),
    INVALID_PASSWORD("10005", "Invalid password", "error.user.invalid_password", 401),
    IDENTIFIER_ALREADY_EXISTS("10006", "Identifier already exists", "error.user.identifier_exists", 409),
    SOCIAL_ALREADY_CONNECTED("10007", "Social provider already connected", "error.user.social_connected", 409),
    SOCIAL_NOT_CONNECTED("10008", "Social provider not connected", "error.user.social_not_connected", 400),
    EMAIL_ALREADY_EXISTS("10009", "Email already exists", "error.user.email_exists", 409),
    USERNAME_ALREADY_EXISTS("10010", "Username already exists", "error.user.username_exists", 409),
    ACCOUNT_LOCKED("10011", "Account is locked", "error.user.account_locked", 423),
    INVALID_STATUS("INVALID_STATUS", "Invalid status for this operation", "error.user.invalid_status", 422),
    USERNAME_ALREADY_CHANGED("10012", "Username has already been changed", "error.user.username_already_changed", 422),
    PHONE_ALREADY_EXISTS("10013", "Phone number already exists", "error.user.phone_exists", 409),
    CURRENT_PASSWORD_REQUIRED("10014", "Current password is required", "error.user.current_password_required", 422);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    UserErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
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
