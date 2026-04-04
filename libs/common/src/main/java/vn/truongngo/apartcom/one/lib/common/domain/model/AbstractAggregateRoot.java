package vn.truongngo.apartcom.one.lib.common.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractAggregateRoot<T extends Id<?>> implements AggregateRoot<T> {

    private final T id;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public AbstractAggregateRoot(T id) {
        this.id = id;
    }

    public void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    @Override
    public T getId() {
        return id;
    }

    @Override
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    @Override
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
