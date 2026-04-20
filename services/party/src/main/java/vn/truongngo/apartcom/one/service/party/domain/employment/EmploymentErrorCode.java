package vn.truongngo.apartcom.one.service.party.domain.employment;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum EmploymentErrorCode implements ErrorCode {

    EMPLOYMENT_NOT_FOUND         ("20201", "Employment not found",                                          "error.employment.not_found",          404),
    EMPLOYMENT_ALREADY_TERMINATED("20202", "Employment already terminated",                                 "error.employment.already_terminated", 422),
    EMPLOYMENT_NOT_ACTIVE        ("20203", "Employment is not active",                                      "error.employment.not_active",         422),
    INVALID_EMPLOYMENT_TARGET    ("20204", "Target organization must be a BQL organization",                "error.employment.invalid_target",     422),
    PERSON_ALREADY_EMPLOYED      ("20205", "Person already has an active employment with this organization","error.employment.person_duplicate",   409);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    EmploymentErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
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
