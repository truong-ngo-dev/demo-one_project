package vn.truongngo.apartcom.one.lib.common.domain.model;

import java.time.Instant;

public abstract class AbstractDomainEvent implements DomainEvent {

    private final String eventId;
    private final String aggregateId;
    private final Instant occurredOn;

    public AbstractDomainEvent(String eventId, String aggregateId, Instant occurredOn) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.occurredOn = occurredOn;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }
}
