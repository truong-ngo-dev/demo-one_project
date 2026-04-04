package vn.truongngo.apartcom.one.service.admin.application.user.find_by_identity;

import vn.truongngo.apartcom.one.service.admin.domain.user.UserStatus;

import java.util.Set;

public record UserView(
        String userId,
        String username,
        String email,
        String phoneNumber,
        String hashedPassword,
        UserStatus status,
        Set<RoleView> roles,
        Set<SocialConnectionView> socialConnections
) {
    public record RoleView(String id, String name) {}
    public record SocialConnectionView(String provider, String socialId) {}
}
