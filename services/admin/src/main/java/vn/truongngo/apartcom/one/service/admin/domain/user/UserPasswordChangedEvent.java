package vn.truongngo.apartcom.one.service.admin.domain.user;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class UserPasswordChangedEvent extends AbstractDomainEvent implements DomainEvent {

    private UserPasswordChangedEvent(String aggregateId) {
        super(UUID.randomUUID().toString(), aggregateId, Instant.now());
    }

    public static UserPasswordChangedEvent of(String userId) {
        return new UserPasswordChangedEvent(userId);
    }
}
