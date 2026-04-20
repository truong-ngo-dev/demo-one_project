package vn.truongngo.apartcom.one.service.party.domain.party.event;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrgType;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

import java.time.Instant;
import java.util.UUID;

@Getter
public class OrganizationCreatedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String name;
    private final OrgType orgType;

    public OrganizationCreatedEvent(PartyId partyId, String name, OrgType orgType) {
        super(UUID.randomUUID().toString(), partyId.getValue(), Instant.now());
        this.name    = name;
        this.orgType = orgType;
    }
}
