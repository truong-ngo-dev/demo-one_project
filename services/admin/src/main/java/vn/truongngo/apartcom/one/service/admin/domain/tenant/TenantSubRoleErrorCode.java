package vn.truongngo.apartcom.one.service.admin.domain.tenant;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum TenantSubRoleErrorCode implements ErrorCode {

    SUB_ROLE_ASSIGNMENT_NOT_FOUND("32001", "Sub-role assignment not found",
            "error.tenant.sub_role.not_found", 404),
    SUB_ROLE_ALREADY_ASSIGNED("32002", "Sub-role already assigned to this user in the org",
            "error.tenant.sub_role.already_assigned", 409),
    ASSIGNER_NOT_AUTHORIZED("32003", "Assigner does not have TENANT context for this org",
            "error.tenant.sub_role.assigner_not_authorized", 403),
    TARGET_USER_NOT_TENANT_MEMBER("32004", "Target user does not have active TENANT context for this org",
            "error.tenant.sub_role.target_not_member", 422);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    TenantSubRoleErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
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
