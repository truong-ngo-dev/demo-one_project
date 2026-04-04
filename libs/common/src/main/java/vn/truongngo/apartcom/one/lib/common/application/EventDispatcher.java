package vn.truongngo.apartcom.one.lib.common.application;

import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.service.EventHandler;

import java.util.*;

public class EventDispatcher {

    private final Map<Class<?>, List<EventHandler<?>>> registry = new HashMap<>();

    public void registerAll(List<EventHandler<?>> handlers) {
        handlers.forEach(this::register);
        registry.values().forEach(list ->
                list.sort(Comparator.comparingInt(EventHandler::getOrder))
        );
    }

    public <E extends DomainEvent> void register(EventHandler<E> handler) {
        registry
                .computeIfAbsent(handler.getEventType(), k -> new ArrayList<>())
                .add(handler);
    }

    @SuppressWarnings("unchecked")
    public void dispatch(DomainEvent event) {
        registry
                .getOrDefault(event.getClass(), List.of())
                .forEach(handler -> ((EventHandler<DomainEvent>) handler).handle(event));
    }

    public void dispatchAll(List<DomainEvent> events) {
        events.forEach(this::dispatch);
    }
}
