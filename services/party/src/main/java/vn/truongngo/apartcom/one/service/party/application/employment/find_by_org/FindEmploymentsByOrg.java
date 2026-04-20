package vn.truongngo.apartcom.one.service.party.application.employment.find_by_org;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.application.employment.EmploymentView;
import vn.truongngo.apartcom.one.service.party.application.employment.PositionAssignmentView;
import vn.truongngo.apartcom.one.service.party.domain.employment.Employment;
import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentRepository;
import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentStatus;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

import java.util.List;

public class FindEmploymentsByOrg {

    public record Query(String orgId, EmploymentStatus status) {
        public Query {
            Assert.hasText(orgId, "orgId is required");
        }
    }

    static class Mapper {
        static EmploymentView toView(Employment emp) {
            List<PositionAssignmentView> positions = emp.getPositions().stream()
                    .map(p -> new PositionAssignmentView(
                            p.getId().toString(),
                            p.getPosition().name(),
                            p.getDepartment(),
                            p.getStartDate(),
                            p.getEndDate()
                    ))
                    .toList();
            return new EmploymentView(
                    emp.getId().getValue(),
                    emp.getRelationshipId().getValue(),
                    emp.getEmployeeId().getValue(),
                    emp.getOrgId().getValue(),
                    emp.getEmploymentType(),
                    emp.getStatus(),
                    emp.getStartDate(),
                    emp.getEndDate(),
                    positions
            );
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<EmploymentView>> {

        private final EmploymentRepository employmentRepository;

        @Override
        public List<EmploymentView> handle(Query query) {
            List<Employment> list = employmentRepository.findByOrgId(PartyId.of(query.orgId()));
            if (query.status() != null) {
                list = list.stream().filter(e -> e.getStatus() == query.status()).toList();
            }
            return list.stream().map(Mapper::toView).toList();
        }
    }
}
