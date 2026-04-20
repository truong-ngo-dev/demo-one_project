package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.employment;

import vn.truongngo.apartcom.one.service.party.domain.employment.*;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRelationshipId;

import java.util.List;
import java.util.UUID;

public class EmploymentMapper {

    public static Employment toDomain(EmploymentJpaEntity entity) {
        List<PositionAssignment> positions = entity.getPositions().stream()
                .map(EmploymentMapper::toDomainPosition)
                .toList();
        return Employment.reconstitute(
                EmploymentId.of(entity.getId()),
                PartyRelationshipId.of(entity.getRelationshipId()),
                PartyId.of(entity.getEmployeeId()),
                PartyId.of(entity.getOrgId()),
                entity.getEmploymentType(),
                entity.getStatus(),
                positions,
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    public static EmploymentJpaEntity toEntity(Employment employment) {
        EmploymentJpaEntity entity = new EmploymentJpaEntity();
        entity.setId(employment.getId().getValue());
        entity.setRelationshipId(employment.getRelationshipId().getValue());
        entity.setEmployeeId(employment.getEmployeeId().getValue());
        entity.setOrgId(employment.getOrgId().getValue());
        entity.setEmploymentType(employment.getEmploymentType());
        entity.setStatus(employment.getStatus());
        entity.setStartDate(employment.getStartDate());
        entity.setEndDate(employment.getEndDate());
        List<PositionAssignmentJpaEntity> posEntities = employment.getPositions().stream()
                .map(p -> toEntityPosition(p, employment.getId().getValue()))
                .toList();
        entity.getPositions().addAll(posEntities);
        return entity;
    }

    public static void updateEntity(EmploymentJpaEntity existing, Employment domain) {
        existing.setStatus(domain.getStatus());
        existing.setEndDate(domain.getEndDate());
        existing.getPositions().clear();
        domain.getPositions().stream()
                .map(p -> toEntityPosition(p, domain.getId().getValue()))
                .forEach(existing.getPositions()::add);
    }

    private static PositionAssignment toDomainPosition(PositionAssignmentJpaEntity entity) {
        return PositionAssignment.reconstitute(
                UUID.fromString(entity.getId()),
                entity.getPosition(),
                entity.getDepartment(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    private static PositionAssignmentJpaEntity toEntityPosition(PositionAssignment pos, String employmentId) {
        PositionAssignmentJpaEntity entity = new PositionAssignmentJpaEntity();
        entity.setId(pos.getId().toString());
        entity.setEmploymentId(employmentId);
        entity.setPosition(pos.getPosition());
        entity.setDepartment(pos.getDepartment());
        entity.setStartDate(pos.getStartDate());
        entity.setEndDate(pos.getEndDate());
        return entity;
    }
}
