package vn.truongngo.apartcom.one.service.party.application.employment.assign_position;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.employment.*;
import vn.truongngo.apartcom.one.service.party.domain.employment.event.PositionAssignedEvent;

import java.time.LocalDate;

public class AssignPosition {

    public record Command(
            String employmentId,
            BQLPosition position,
            String department,
            LocalDate startDate
    ) {
        public Command {
            Assert.hasText(employmentId, "employmentId is required");
            Assert.notNull(position, "position is required");
            Assert.notNull(startDate, "startDate is required");
        }
    }

    public record Result(String positionAssignmentId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final EmploymentRepository employmentRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            Employment emp = employmentRepository.findById(EmploymentId.of(command.employmentId()))
                    .orElseThrow(EmploymentException::notFound);

            PositionAssignment pos = emp.assignPosition(command.position(), command.department(), command.startDate());
            employmentRepository.save(emp);

            eventDispatcher.dispatch(new PositionAssignedEvent(emp.getId(), command.position(),
                    command.department(), command.startDate()));

            return new Result(pos.getId().toString());
        }
    }
}
