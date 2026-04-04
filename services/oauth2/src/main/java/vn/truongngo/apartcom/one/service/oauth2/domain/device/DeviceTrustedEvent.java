package vn.truongngo.apartcom.one.service.oauth2.domain.device;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class DeviceTrustedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String userId;

    public DeviceTrustedEvent(String eventId, String aggregateId, Instant occurredOn, String userId) {
        super(eventId, aggregateId, occurredOn);
        this.userId = userId;
    }

    public DeviceTrustedEvent(String aggregateId, String userId) {
        this(UUID.randomUUID().toString(), aggregateId, Instant.now(), userId);
    }
}
