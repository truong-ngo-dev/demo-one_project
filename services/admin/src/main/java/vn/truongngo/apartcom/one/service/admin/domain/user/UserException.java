package vn.truongngo.apartcom.one.service.admin.domain.user;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class UserException extends DomainException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }

    public static UserException notFound() {
        return new UserException(UserErrorCode.USER_NOT_FOUND);
    }

    public static UserException emailAlreadyExists() {
        return new UserException(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }

    public static UserException usernameAlreadyExists() {
        return new UserException(UserErrorCode.USERNAME_ALREADY_EXISTS);
    }

    public static UserException locked() {
        return new UserException(UserErrorCode.ACCOUNT_LOCKED);
    }

    public static UserException invalidStatus() {
        return new UserException(UserErrorCode.INVALID_STATUS);
    }

    public static UserException usernameAlreadyChanged() {
        return new UserException(UserErrorCode.USERNAME_ALREADY_CHANGED);
    }

    public static UserException phoneAlreadyExists() {
        return new UserException(UserErrorCode.PHONE_ALREADY_EXISTS);
    }

    public static UserException currentPasswordRequired() {
        return new UserException(UserErrorCode.CURRENT_PASSWORD_REQUIRED);
    }

    public static UserException invalidPassword() {
        return new UserException(UserErrorCode.INVALID_PASSWORD);
    }
}
