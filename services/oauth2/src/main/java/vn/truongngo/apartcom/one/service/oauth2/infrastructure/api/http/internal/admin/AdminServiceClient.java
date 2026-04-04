package vn.truongngo.apartcom.one.service.oauth2.infrastructure.api.http.internal.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import vn.truongngo.apartcom.one.lib.shared.dto.user.SocialRegisterResponse;
import vn.truongngo.apartcom.one.lib.shared.dto.user.UserIdentityResponse;

/**
 * HTTP client cho Admin Service — gom tất cả outbound call sang admin-service.
 * DTOs dùng từ libs/shared (shared contract giữa oauth2 và admin service).
 */
@Component
@RequiredArgsConstructor
public class AdminServiceClient {

    private final RestClient adminRestClient;

    /** UC: Form login — lấy identity user theo username hoặc email. */
    public UserIdentityResponse getUserIdentity(String usernameOrEmail) {
        return adminRestClient.get()
                .uri("/api/v1/internal/users/identity?value={value}", usernameOrEmail)
                .retrieve()
                .body(UserIdentityResponse.class);
    }

    /** UC: Social login — register hoặc find user theo provider. */
    public SocialRegisterResponse registerSocialUser(String provider, String providerUserId, String providerEmail) {
        return adminRestClient.post()
                .uri("/api/v1/internal/users/social")
                .body(new SocialRegisterRequest(provider, providerUserId, providerEmail))
                .retrieve()
                .body(SocialRegisterResponse.class);
    }

    private record SocialRegisterRequest(String provider, String providerUserId, String providerEmail) {}
}
