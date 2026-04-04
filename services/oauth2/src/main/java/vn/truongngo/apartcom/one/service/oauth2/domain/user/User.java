package vn.truongngo.apartcom.one.service.oauth2.domain.user;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;

import java.util.Set;

/**
 * Projection of User from admin-service into oauth2's bounded context.
 * Immutable, no domain events — cross-BC read-only reference.
 * Not a locally-owned aggregate: oauth2 never creates or mutates user state.
 */
@Getter
public final class User {

    private final UserId id;
    private final String username;
    private final String passwordHash;   // null for social-only users
    private final UserStatus status;
    private final Set<String> roles;
    private final boolean requiresProfileCompletion;

    private User(
            UserId id,
            String username,
            String passwordHash,
            UserStatus status,
            Set<String> roles,
            boolean requiresProfileCompletion) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = status;
        this.roles = Set.copyOf(roles);
        this.requiresProfileCompletion = requiresProfileCompletion;
    }

    /** Form login — user resolved by credentials. */
    public static User fromIdentity(
            UserId id,
            String username,
            String passwordHash,
            UserStatus status,
            Set<String> roles) {
        return new User(id, username, passwordHash, status, roles, false);
    }

    /** Social login — user resolved (or created) by social identity. */
    public static User fromSocialRegistration(
            UserId id,
            String username,
            boolean requiresProfileCompletion) {
        return new User(id, username, null, UserStatus.ACTIVE, Set.of(), requiresProfileCompletion);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isLocked() {
        return status == UserStatus.LOCKED;
    }
}
