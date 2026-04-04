package vn.truongngo.apartcom.one.service.oauth2.domain.session;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
public class SessionRevokedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String userId;
    private final String deviceId;
    private final String idpSessionId;

    /**
     * @param aggregateId Đây chính là authorizationId (sid) để báo cho Web Gateway
     */
    public SessionRevokedEvent(String eventId, String aggregateId, Instant occurredOn,
                               String userId, String deviceId, String idpSessionId) {
        super(eventId, aggregateId, occurredOn);
        this.userId = userId;
        this.deviceId = deviceId;
        this.idpSessionId = idpSessionId;
    }

    public SessionRevokedEvent(String aggregateId, String userId, String deviceId, String idpSessionId) {
        this(UUID.randomUUID().toString(), aggregateId, Instant.now(), userId, deviceId, idpSessionId);
    }
}
