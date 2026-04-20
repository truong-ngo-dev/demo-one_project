package vn.truongngo.apartcom.one.service.admin.domain.tenant;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class TenantSubRoleException extends DomainException {

    public TenantSubRoleException(TenantSubRoleErrorCode errorCode) {
        super(errorCode);
    }

    public static TenantSubRoleException notFound() {
        return new TenantSubRoleException(TenantSubRoleErrorCode.SUB_ROLE_ASSIGNMENT_NOT_FOUND);
    }

    public static TenantSubRoleException alreadyAssigned() {
        return new TenantSubRoleException(TenantSubRoleErrorCode.SUB_ROLE_ALREADY_ASSIGNED);
    }

    public static TenantSubRoleException assignerNotAuthorized() {
        return new TenantSubRoleException(TenantSubRoleErrorCode.ASSIGNER_NOT_AUTHORIZED);
    }

    public static TenantSubRoleException targetUserNotTenantMember() {
        return new TenantSubRoleException(TenantSubRoleErrorCode.TARGET_USER_NOT_TENANT_MEMBER);
    }
}
