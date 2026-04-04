package vn.truongngo.apartcom.one.service.admin.domain.user;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class SocialConnection {

    private final String provider;
    private final String socialId;
    private final String email;
    private final Instant connectedAt;

    SocialConnection(String provider, String socialId, String email, Instant connectedAt) {
        this.provider    = provider;
        this.socialId    = socialId;
        this.email       = email;
        this.connectedAt = connectedAt;
    }

    // For infrastructure — reconstitute từ persistence, không phải tạo mới qua domain behavior
    public static SocialConnection reconstitute(String provider, String socialId, String email, Instant connectedAt) {
        return new SocialConnection(provider, socialId, email, connectedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocialConnection that)) return false;
        return provider.equals(that.provider) && socialId.equals(that.socialId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, socialId);
    }

    public boolean matches(String provider, String socialId) {
        return this.provider.equals(provider) && this.socialId.equals(socialId);
    }
}
