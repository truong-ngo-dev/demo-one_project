package vn.truongngo.apartcom.one.service.party.domain.party_relationship;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

import java.time.LocalDate;

@Getter
public class PartyRelationship extends AbstractAggregateRoot<PartyRelationshipId>
        implements AggregateRoot<PartyRelationshipId> {

    private final PartyId fromPartyId;
    private final PartyId toPartyId;
    private final PartyRelationshipType type;
    private final PartyRoleType fromRole;
    private final PartyRoleType toRole;
    private PartyRelationshipStatus status;
    private final LocalDate startDate;
    private LocalDate endDate;

    private PartyRelationship(PartyRelationshipId id, PartyId fromPartyId, PartyId toPartyId,
                              PartyRelationshipType type, PartyRoleType fromRole, PartyRoleType toRole,
                              PartyRelationshipStatus status, LocalDate startDate, LocalDate endDate) {
        super(id);
        this.fromPartyId = fromPartyId;
        this.toPartyId   = toPartyId;
        this.type        = type;
        this.fromRole    = fromRole;
        this.toRole      = toRole;
        this.status      = status;
        this.startDate   = startDate;
        this.endDate     = endDate;
    }

    public static PartyRelationship create(PartyId fromPartyId, PartyId toPartyId,
                                           PartyRelationshipType type,
                                           PartyRoleType fromRole, PartyRoleType toRole,
                                           LocalDate startDate) {
        Assert.notNull(fromPartyId, "fromPartyId is required");
        Assert.notNull(toPartyId, "toPartyId is required");
        Assert.notNull(type, "type is required");
        Assert.notNull(fromRole, "fromRole is required");
        Assert.notNull(toRole, "toRole is required");
        Assert.notNull(startDate, "startDate is required");
        return new PartyRelationship(PartyRelationshipId.generate(), fromPartyId, toPartyId,
                type, fromRole, toRole, PartyRelationshipStatus.ACTIVE, startDate, null);
    }

    public static PartyRelationship reconstitute(PartyRelationshipId id, PartyId fromPartyId,
                                                 PartyId toPartyId, PartyRelationshipType type,
                                                 PartyRoleType fromRole, PartyRoleType toRole,
                                                 PartyRelationshipStatus status,
                                                 LocalDate startDate, LocalDate endDate) {
        return new PartyRelationship(id, fromPartyId, toPartyId, type, fromRole, toRole,
                status, startDate, endDate);
    }

    // ── Behaviors ────────────────────────────────────────────────────────────

    public void end(LocalDate endDate) {
        if (this.status == PartyRelationshipStatus.ENDED) {
            throw PartyRelationshipException.alreadyEnded();
        }
        this.status  = PartyRelationshipStatus.ENDED;
        this.endDate = endDate;
    }
}
