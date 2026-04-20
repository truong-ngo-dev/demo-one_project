package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.reference;

import vn.truongngo.apartcom.one.service.admin.domain.reference.OrgReference;

public class OrgReferenceMapper {

    public static OrgReference toDomain(OrgReferenceJpaEntity entity) {
        return OrgReference.reconstitute(
                entity.getOrgId(),
                entity.getName(),
                entity.getOrgType(),
                entity.getCachedAt()
        );
    }

    public static OrgReferenceJpaEntity toEntity(OrgReference domain) {
        OrgReferenceJpaEntity entity = new OrgReferenceJpaEntity();
        entity.setOrgId(domain.getOrgId());
        entity.setName(domain.getName());
        entity.setOrgType(domain.getOrgType());
        entity.setCachedAt(domain.getCachedAt());
        return entity;
    }
}
