package vn.truongngo.apartcom.one.lib.common.domain.service;

import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;

public interface EventHandler<E extends DomainEvent> {
    void handle(E event);

    Class<E> getEventType();

    default int getOrder() {
        return 100;
    }
}
