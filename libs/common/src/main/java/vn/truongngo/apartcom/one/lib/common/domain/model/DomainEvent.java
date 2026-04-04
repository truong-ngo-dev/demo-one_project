package vn.truongngo.apartcom.one.lib.common.domain.model;

import java.time.Instant;

public interface DomainEvent {
    String getEventId();
    Instant getOccurredOn();
    String getAggregateId();
}
