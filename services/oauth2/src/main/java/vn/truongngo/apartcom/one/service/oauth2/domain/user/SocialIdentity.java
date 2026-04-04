package vn.truongngo.apartcom.one.service.oauth2.domain.user;

import vn.truongngo.apartcom.one.lib.common.domain.model.ValueObject;

public record SocialIdentity(
        String provider,
        String providerUserId,
        String providerEmail
) implements ValueObject {}
