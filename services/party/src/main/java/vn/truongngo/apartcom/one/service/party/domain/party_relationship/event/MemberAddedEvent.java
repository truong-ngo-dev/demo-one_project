package vn.truongngo.apartcom.one.service.party.domain.party_relationship.event;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyType;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRelationshipId;

import java.time.Instant;
import java.util.UUID;

@Getter
public class MemberAddedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String relationshipId;
    private final String personId;
    private final String groupId;
    private final PartyType groupType;

    public MemberAddedEvent(PartyRelationshipId relId, PartyId personId, PartyId groupId, PartyType groupType) {
        super(UUID.randomUUID().toString(), relId.getValue(), Instant.now());
        this.relationshipId = relId.getValue();
        this.personId       = personId.getValue();
        this.groupId        = groupId.getValue();
        this.groupType      = groupType;
    }
}
