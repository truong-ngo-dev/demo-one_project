package vn.truongngo.apartcom.one.service.oauth2.infrastructure.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import vn.truongngo.apartcom.one.service.oauth2.domain.user.SocialIdentity;
import vn.truongngo.apartcom.one.service.oauth2.domain.user.User;
import vn.truongngo.apartcom.one.service.oauth2.domain.user.UserIdentityService;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles social login via OIDC providers (e.g. Google).
 * After the external provider authenticates the user:
 * 1. Calls UserIdentityService.resolveBySocialIdentity() — find-or-create via admin-service.
 * 2. Overrides the OIDC principal `sub` with the admin-service userId.
 * 3. Adds `requires_profile_completion` to the principal claims
 *    so the JWT customizer can include it in the issued access token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoginOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final String CLAIM_REQUIRES_PROFILE_COMPLETION = "requires_profile_completion";

    private final UserIdentityService userRepository;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser googleUser = delegate.loadUser(userRequest);

        String provider       = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String providerUserId = googleUser.getSubject();
        String providerEmail  = googleUser.getEmail();

        User user = resolveSocialUser(new SocialIdentity(provider, providerUserId, providerEmail));

        OidcIdToken enrichedToken = buildEnrichedToken(googleUser, user);

        return new DefaultOidcUser(googleUser.getAuthorities(), enrichedToken, googleUser.getUserInfo());
    }

    private User resolveSocialUser(SocialIdentity identity) {
        try {
            return userRepository.resolveBySocialIdentity(identity);
        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SocialLogin] Failed to resolve social user: {}", e.getMessage());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("admin_service_error"), "Failed to resolve social user: " + e.getMessage());
        }
    }

    private static OidcIdToken buildEnrichedToken(OidcUser googleUser, User user) {
        Map<String, Object> claims = new HashMap<>(googleUser.getIdToken().getClaims());
        // Replace Google's sub with admin-service userId so JWT `sub` = userId
        claims.put("sub", user.getId().getValueAsString());
        claims.put(CLAIM_REQUIRES_PROFILE_COMPLETION, user.isRequiresProfileCompletion());

        return new OidcIdToken(
                googleUser.getIdToken().getTokenValue(),
                googleUser.getIdToken().getIssuedAt(),
                googleUser.getIdToken().getExpiresAt(),
                claims
        );
    }
}
