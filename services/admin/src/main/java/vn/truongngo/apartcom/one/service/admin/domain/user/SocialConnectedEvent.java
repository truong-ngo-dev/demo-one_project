package vn.truongngo.apartcom.one.service.admin.domain.user;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
public class SocialConnectedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String provider;
    private final String socialId;

    private SocialConnectedEvent(String aggregateId,
                                 String provider,
                                 String socialId) {
        super(UUID.randomUUID().toString(), aggregateId, Instant.now());
        this.provider = provider;
        this.socialId = socialId;
    }

    public static SocialConnectedEvent of(String userId,
                                          String provider,
                                          String socialId) {
        return new SocialConnectedEvent(userId, provider, socialId);
    }

}
