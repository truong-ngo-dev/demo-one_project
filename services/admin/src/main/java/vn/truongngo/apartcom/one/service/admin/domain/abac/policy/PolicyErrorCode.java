package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum PolicyErrorCode implements ErrorCode {

    POLICY_NOT_FOUND("30009", "Policy not found", "error.abac.policy.not_found", 404),
    RULE_NOT_FOUND("30010", "Rule not found", "error.abac.rule.not_found", 404),
    INVALID_SPEL_EXPRESSION("30011", "Invalid SpEL expression", "error.abac.invalid_spel", 400),
    DUPLICATE_POLICY_NAME("30012", "Duplicate policy name", "error.abac.duplicate_name", 400);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    PolicyErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
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
