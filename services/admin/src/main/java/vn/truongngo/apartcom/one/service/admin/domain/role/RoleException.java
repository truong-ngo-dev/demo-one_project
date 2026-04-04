package vn.truongngo.apartcom.one.service.admin.domain.role;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class RoleException extends DomainException {

    public RoleException(RoleErrorCode errorCode) {
        super(errorCode);
    }

    public static RoleException notFound() {
        return new RoleException(RoleErrorCode.ROLE_NOT_FOUND);
    }

    public static RoleException alreadyExists() {
        return new RoleException(RoleErrorCode.ROLE_ALREADY_EXISTS);
    }

    public static RoleException nameImmutable() {
        return new RoleException(RoleErrorCode.ROLE_NAME_IMMUTABLE);
    }

    public static RoleException inUse() {
        return new RoleException(RoleErrorCode.ROLE_IN_USE);
    }
}
