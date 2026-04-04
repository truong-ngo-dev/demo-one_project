package vn.truongngo.apartcom.one.lib.shared.dto.user;

import java.util.Set;

/**
 * Shared DTO for the internal user identity endpoint.
 * Admin service produces this, oauth2 service consumes this.
 */
public record UserIdentityResponse(
        String userId,
        String username,
        String email,
        String phoneNumber,
        String passwordHash,   // BCrypt hash — null for social-only users
        String status,         // "ACTIVE", "PENDING", "LOCKED"
        Set<String> roles      // role names
) {}
