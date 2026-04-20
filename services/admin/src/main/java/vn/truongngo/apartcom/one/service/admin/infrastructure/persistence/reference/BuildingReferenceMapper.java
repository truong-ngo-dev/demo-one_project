package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.reference;

import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReference;

public class BuildingReferenceMapper {

    public static BuildingReference toDomain(BuildingReferenceJpaEntity entity) {
        return BuildingReference.reconstitute(
                entity.getBuildingId(),
                entity.getName(),
                entity.getManagingOrgId(),
                entity.getCachedAt()
        );
    }

    public static BuildingReferenceJpaEntity toEntity(BuildingReference domain) {
        BuildingReferenceJpaEntity entity = new BuildingReferenceJpaEntity();
        entity.setBuildingId(domain.getBuildingId());
        entity.setName(domain.getName());
        entity.setManagingOrgId(domain.getManagingOrgId());
        entity.setCachedAt(domain.getCachedAt());
        return entity;
    }
}
