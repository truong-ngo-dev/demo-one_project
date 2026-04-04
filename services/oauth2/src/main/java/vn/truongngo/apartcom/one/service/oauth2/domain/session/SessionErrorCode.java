package vn.truongngo.apartcom.one.service.oauth2.domain.session;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum SessionErrorCode implements ErrorCode {

    /**
     * args: none
     */
    SESSION_NOT_FOUND(
            "02001",
            "Session not found",
            "error.session.not_found",
            404
    ),

    /**
     * args: none
     */
    SESSION_NOT_ACTIVE(
            "02002",
            "Session is not active",
            "error.session.not_active",
            409
    ),

    /**
     * args: none
     */
    SESSION_ALREADY_REVOKED(
            "02003",
            "Session is already revoked",
            "error.session.already_revoked",
            409
    ),

    /**
     * args: none
     */
    SESSION_EXPIRED(
            "02004",
            "Session has expired",
            "error.session.expired",
            401
    ),

    /**
     * args: none
     */
    SESSION_NOT_BELONG_TO_USER(
            "02005",
            "Session does not belong to user",
            "error.session.not_belong_to_user",
            403
    ),

    /**
     * args: none
     * Dùng khi user cố tự đăng xuất session hiện tại qua remote revoke.
     * Phải dùng logout chuẩn (UC-005).
     */
    CANNOT_REVOKE_CURRENT_SESSION(
            "02006",
            "Cannot revoke your current session via remote logout. Use standard logout instead.",
            "error.session.cannot_revoke_current",
            400
    );

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    SessionErrorCode(
            String code,
            String defaultMessage,
            String messageKey,
            int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }
}