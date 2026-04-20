package vn.truongngo.apartcom.one.service.party.domain.employment;

import vn.truongngo.apartcom.one.lib.common.domain.service.Repository;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

import java.util.List;

public interface EmploymentRepository extends Repository<Employment, EmploymentId> {

    List<Employment> findByOrgId(PartyId orgId);

    List<Employment> findByEmployeeId(PartyId employeeId);

    boolean existsActiveByEmployeeIdAndOrgId(PartyId employeeId, PartyId orgId);
}
