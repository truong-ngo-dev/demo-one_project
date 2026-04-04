package vn.truongngo.apartcom.one.service.admin.domain.role;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum RoleErrorCode implements ErrorCode {

    ROLE_NOT_FOUND("20001", "Role not found", "error.role.not_found", 404),
    ROLE_ALREADY_EXISTS("20002", "Role name already exists", "error.role.already_exists", 409),
    ROLE_NAME_IMMUTABLE("20003", "Role name cannot be changed", "error.role.name_immutable", 422),
    ROLE_IN_USE("20004", "Role is assigned to one or more users", "error.role.in_use", 409);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    RoleErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
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
