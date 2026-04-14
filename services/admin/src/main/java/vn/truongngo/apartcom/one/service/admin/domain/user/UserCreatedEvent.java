package vn.truongngo.apartcom.one.service.admin.domain.user;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserCreatedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String username;
    private final String email;
    private final String phoneNumber;
    private final RegistrationMethod registrationMethod;

    public enum RegistrationMethod {
        DEFAULT,
        ADMIN,
        SOCIAL
    }

    public UserCreatedEvent(String aggregateId,
                            String username,
                            String email,
                            String phoneNumber,
                            RegistrationMethod registrationMethod) {
        super(UUID.randomUUID().toString(), aggregateId, Instant.now());
        this.username           = username;
        this.email              = email;
        this.phoneNumber        = phoneNumber;
        this.registrationMethod = registrationMethod;
    }
}
