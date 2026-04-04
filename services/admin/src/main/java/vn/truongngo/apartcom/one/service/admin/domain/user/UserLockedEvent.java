package vn.truongngo.apartcom.one.service.admin.domain.user;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class UserLockedEvent extends AbstractDomainEvent implements DomainEvent {

    public UserLockedEvent(String eventId, String aggregateId, Instant occurredOn) {
        super(eventId, aggregateId, occurredOn);
    }

    public UserLockedEvent(String aggregateId, Instant occurredOn) {
        this(UUID.randomUUID().toString(), aggregateId, occurredOn);
    }
}
