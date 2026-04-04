package vn.truongngo.apartcom.one.lib.common.domain.model;

import java.util.List;

public interface AggregateRoot<T extends Id<?>> extends Entity<T> {
    List<DomainEvent> getDomainEvents();
    List<DomainEvent> pullDomainEvents();
    void clearDomainEvents();
}
