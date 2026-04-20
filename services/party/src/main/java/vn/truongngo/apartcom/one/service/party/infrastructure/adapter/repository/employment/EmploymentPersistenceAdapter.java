package vn.truongngo.apartcom.one.service.party.infrastructure.adapter.repository.employment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.party.domain.employment.*;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.employment.EmploymentJpaEntity;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.employment.EmploymentJpaRepository;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.employment.EmploymentMapper;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmploymentPersistenceAdapter implements EmploymentRepository {

    private final EmploymentJpaRepository jpaRepository;

    @Override
    public Optional<Employment> findById(EmploymentId id) {
        return jpaRepository.findById(id.getValue()).map(EmploymentMapper::toDomain);
    }

    @Override
    public void save(Employment employment) {
        Optional<EmploymentJpaEntity> existing = jpaRepository.findById(employment.getId().getValue());
        if (existing.isPresent()) {
            EmploymentMapper.updateEntity(existing.get(), employment);
            jpaRepository.save(existing.get());
        } else {
            jpaRepository.save(EmploymentMapper.toEntity(employment));
        }
    }

    @Override
    public void delete(EmploymentId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public List<Employment> findByOrgId(PartyId orgId) {
        return jpaRepository.findByOrgId(orgId.getValue()).stream()
                .map(EmploymentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Employment> findByEmployeeId(PartyId employeeId) {
        return jpaRepository.findByEmployeeId(employeeId.getValue()).stream()
                .map(EmploymentMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByEmployeeIdAndOrgId(PartyId employeeId, PartyId orgId) {
        return jpaRepository.existsByEmployeeIdAndOrgIdAndStatus(
                employeeId.getValue(), orgId.getValue(), EmploymentStatus.ACTIVE);
    }
}
