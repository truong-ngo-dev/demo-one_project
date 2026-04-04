package vn.truongngo.apartcom.one.service.oauth2.domain.user;

import java.util.Optional;

/**
 * Port: resolves User from admin-service by credentials or social identity.
 * Not a standard CRUD repository — User is a cross-BC read projection, not a locally-owned aggregate.
 */
public interface UserIdentityService {

    /**
     * Resolve user by username or email for form login.
     * Returns empty if user does not exist in admin-service.
     */
    Optional<User> findByCredentials(String usernameOrEmail);

    /**
     * Resolve (or create) user in admin-service by social identity.
     * Always returns a User — admin-service guarantees find-or-create semantics.
     */
    User resolveBySocialIdentity(SocialIdentity identity);
}
