package vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum PolicySetErrorCode implements ErrorCode {

    POLICY_SET_NOT_FOUND("30007", "Policy set not found", "error.abac.policy_set.not_found", 404),
    POLICY_SET_NAME_DUPLICATE("30008", "Policy set name already exists", "error.abac.policy_set.name_duplicate", 409);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    PolicySetErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
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
