package vn.truongngo.apartcom.one.service.admin.domain.user;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class UserUnlockedEvent extends AbstractDomainEvent implements DomainEvent {

    public UserUnlockedEvent(String eventId, String aggregateId, Instant occurredOn) {
        super(eventId, aggregateId, occurredOn);
    }

    public UserUnlockedEvent(String aggregateId) {
        super(UUID.randomUUID().toString(), aggregateId, Instant.now());
    }

}
