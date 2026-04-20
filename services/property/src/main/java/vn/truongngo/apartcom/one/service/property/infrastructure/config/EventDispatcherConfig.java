package vn.truongngo.apartcom.one.service.property.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.domain.service.EventHandler;

import java.util.List;

@Configuration
public class EventDispatcherConfig {

    @Bean
    public EventDispatcher eventDispatcher(List<EventHandler<?>> eventHandlers) {
        EventDispatcher eventDispatcher = new EventDispatcher();
        eventDispatcher.registerAll(eventHandlers);
        return eventDispatcher;
    }
}
