package vn.truongngo.apartcom.one.service.party.domain.employment.event;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;
import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentId;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

import java.time.Instant;
import java.util.UUID;

@Getter
public class EmploymentTerminatedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String employmentId;
    private final String personId;
    private final String orgId;

    public EmploymentTerminatedEvent(EmploymentId employmentId, PartyId personId, PartyId orgId) {
        super(UUID.randomUUID().toString(), employmentId.getValue(), Instant.now());
        this.employmentId = employmentId.getValue();
        this.personId     = personId.getValue();
        this.orgId        = orgId.getValue();
    }
}
