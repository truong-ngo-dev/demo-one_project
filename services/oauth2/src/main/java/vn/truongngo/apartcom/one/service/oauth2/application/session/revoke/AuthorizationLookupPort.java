package vn.truongngo.apartcom.one.service.oauth2.application.session.revoke;

import java.util.Optional;

/**
 * Port — tìm Authorization Record theo token value, trả về authorizationId.
 * Defined tại slice revoke/ vì hiện chỉ RevokeSession cần lookup by token.
 * Nếu slice khác cần, promote lên application/session/ level.
 */
public interface AuthorizationLookupPort {
    Optional<String> findAuthorizationId(String tokenValue, AuthorizationTokenType tokenType);
}
