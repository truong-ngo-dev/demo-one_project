package vn.truongngo.apartcom.one.service.oauth2.domain.device;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
public class DeviceRevokedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String userId;

    public DeviceRevokedEvent(String eventId, String aggregateId, Instant occurredOn, String userId) {
        super(eventId, aggregateId, occurredOn);
        this.userId = userId;
    }

    public DeviceRevokedEvent(String aggregateId, String userId) {
        this(UUID.randomUUID().toString(), aggregateId, Instant.now(), userId);
    }
}
