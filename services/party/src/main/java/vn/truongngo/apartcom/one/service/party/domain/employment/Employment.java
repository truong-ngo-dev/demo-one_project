package vn.truongngo.apartcom.one.service.party.domain.employment;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRelationshipId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Employment extends AbstractAggregateRoot<EmploymentId> implements AggregateRoot<EmploymentId> {

    private final PartyRelationshipId relationshipId;
    private final PartyId employeeId;
    private final PartyId orgId;
    private final EmploymentType employmentType;
    private EmploymentStatus status;
    private final List<PositionAssignment> positions;
    private final LocalDate startDate;
    private LocalDate endDate;

    private Employment(EmploymentId id, PartyRelationshipId relationshipId,
                       PartyId employeeId, PartyId orgId,
                       EmploymentType employmentType, EmploymentStatus status,
                       List<PositionAssignment> positions,
                       LocalDate startDate, LocalDate endDate) {
        super(id);
        this.relationshipId = relationshipId;
        this.employeeId     = employeeId;
        this.orgId          = orgId;
        this.employmentType = employmentType;
        this.status         = status;
        this.positions      = new ArrayList<>(positions);
        this.startDate      = startDate;
        this.endDate        = endDate;
    }

    public static Employment create(PartyRelationshipId relationshipId, PartyId employeeId,
                                    PartyId orgId, EmploymentType employmentType, LocalDate startDate) {
        Assert.notNull(relationshipId, "relationshipId is required");
        Assert.notNull(employeeId, "employeeId is required");
        Assert.notNull(orgId, "orgId is required");
        Assert.notNull(employmentType, "employmentType is required");
        Assert.notNull(startDate, "startDate is required");
        return new Employment(EmploymentId.generate(), relationshipId, employeeId, orgId,
                employmentType, EmploymentStatus.ACTIVE, new ArrayList<>(), startDate, null);
    }

    public static Employment reconstitute(EmploymentId id, PartyRelationshipId relationshipId,
                                          PartyId employeeId, PartyId orgId,
                                          EmploymentType employmentType, EmploymentStatus status,
                                          List<PositionAssignment> positions,
                                          LocalDate startDate, LocalDate endDate) {
        return new Employment(id, relationshipId, employeeId, orgId,
                employmentType, status, positions, startDate, endDate);
    }

    public List<PositionAssignment> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    // ── Behaviors ────────────────────────────────────────────────────────────

    public void terminate(LocalDate endDate) {
        if (this.status == EmploymentStatus.TERMINATED) {
            throw EmploymentException.alreadyTerminated();
        }
        this.status  = EmploymentStatus.TERMINATED;
        this.endDate = endDate;
        positions.stream()
                .filter(p -> p.getEndDate() == null)
                .forEach(p -> p.close(endDate));
    }

    public PositionAssignment assignPosition(BQLPosition position, String department, LocalDate startDate) {
        if (this.status != EmploymentStatus.ACTIVE) {
            throw EmploymentException.notActive();
        }
        PositionAssignment pos = PositionAssignment.create(position, department, startDate);
        positions.add(pos);
        return pos;
    }
}
