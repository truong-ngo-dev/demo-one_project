package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.employment;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentStatus;

import java.util.List;

public interface EmploymentJpaRepository extends JpaRepository<EmploymentJpaEntity, String> {

    List<EmploymentJpaEntity> findByOrgId(String orgId);

    List<EmploymentJpaEntity> findByEmployeeId(String employeeId);

    boolean existsByEmployeeIdAndOrgIdAndStatus(String employeeId, String orgId, EmploymentStatus status);
}
