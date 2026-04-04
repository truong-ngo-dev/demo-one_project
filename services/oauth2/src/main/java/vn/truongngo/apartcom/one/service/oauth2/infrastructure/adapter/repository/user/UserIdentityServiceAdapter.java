package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.repository.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;
import vn.truongngo.apartcom.one.lib.shared.dto.user.SocialRegisterResponse;
import vn.truongngo.apartcom.one.lib.shared.dto.user.UserIdentityResponse;
import vn.truongngo.apartcom.one.service.oauth2.domain.user.SocialIdentity;
import vn.truongngo.apartcom.one.service.oauth2.domain.user.User;
import vn.truongngo.apartcom.one.service.oauth2.domain.user.UserIdentityService;
import vn.truongngo.apartcom.one.service.oauth2.domain.user.UserStatus;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.api.http.internal.admin.AdminServiceClient;

import java.util.Optional;

/**
 * Implements UserIdentityService by delegating to AdminServiceClient (HTTP).
 * Maps admin-service DTOs to the oauth2 User domain object.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserIdentityServiceAdapter implements UserIdentityService {

    private final AdminServiceClient adminServiceClient;

    @Override
    public Optional<User> findByCredentials(String usernameOrEmail) {
        try {
            UserIdentityResponse response = adminServiceClient.getUserIdentity(usernameOrEmail);
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(toUser(response));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public User resolveBySocialIdentity(SocialIdentity identity) {
        SocialRegisterResponse response = adminServiceClient.registerSocialUser(
                identity.provider(),
                identity.providerUserId(),
                identity.providerEmail()
        );
        return User.fromSocialRegistration(
                new UserId(response.userId()),
                response.username(),
                response.requiresProfileCompletion()
        );
    }

    private static User toUser(UserIdentityResponse response) {
        return User.fromIdentity(
                new UserId(response.userId()),
                response.username(),
                response.passwordHash(),
                UserStatus.valueOf(response.status()),
                response.roles()
        );
    }
}
