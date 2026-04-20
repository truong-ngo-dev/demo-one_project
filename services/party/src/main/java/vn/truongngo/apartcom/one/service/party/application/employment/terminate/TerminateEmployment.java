package vn.truongngo.apartcom.one.service.party.application.employment.terminate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.employment.*;
import vn.truongngo.apartcom.one.service.party.domain.employment.event.EmploymentTerminatedEvent;

import java.time.LocalDate;

public class TerminateEmployment {

    public record Command(String employmentId, LocalDate endDate) {
        public Command {
            Assert.hasText(employmentId, "employmentId is required");
            Assert.notNull(endDate, "endDate is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final EmploymentRepository employmentRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Void handle(Command command) {
            Employment emp = employmentRepository.findById(EmploymentId.of(command.employmentId()))
                    .orElseThrow(EmploymentException::notFound);

            emp.terminate(command.endDate());
            employmentRepository.save(emp);

            eventDispatcher.dispatch(new EmploymentTerminatedEvent(emp.getId(), emp.getEmployeeId(), emp.getOrgId()));

            return null;
        }
    }
}
