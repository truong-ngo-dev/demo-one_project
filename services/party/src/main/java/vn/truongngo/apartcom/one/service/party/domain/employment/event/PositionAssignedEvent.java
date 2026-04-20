package vn.truongngo.apartcom.one.service.party.domain.employment.event;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;
import vn.truongngo.apartcom.one.service.party.domain.employment.BQLPosition;
import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class PositionAssignedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String employmentId;
    private final String position;
    private final String department;
    private final LocalDate startDate;

    public PositionAssignedEvent(EmploymentId employmentId, BQLPosition position,
                                 String department, LocalDate startDate) {
        super(UUID.randomUUID().toString(), employmentId.getValue(), Instant.now());
        this.employmentId = employmentId.getValue();
        this.position     = position.name();
        this.department   = department;
        this.startDate    = startDate;
    }
}
