package vn.truongngo.apartcom.one.lib.shared.dto.user;

/**
 * Shared DTO for the internal social register endpoint.
 * Admin service produces this, oauth2 service consumes this.
 */
public record SocialRegisterResponse(
        String userId,
        String username,
        boolean requiresProfileCompletion
) {}
