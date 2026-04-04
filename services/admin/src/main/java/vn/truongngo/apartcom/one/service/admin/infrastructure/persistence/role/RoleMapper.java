package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role;

import vn.truongngo.apartcom.one.lib.common.domain.model.Auditable;
import vn.truongngo.apartcom.one.service.admin.domain.role.Role;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;

public class RoleMapper {

    public static Role toDomain(RoleJpaEntity entity) {
        Auditable auditable = new Auditable(
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy()
        );
        return Role.reconstitute(RoleId.of(entity.getId()), entity.getName(), entity.getDescription(), auditable);
    }

    public static RoleJpaEntity toEntity(Role role) {
        RoleJpaEntity entity = new RoleJpaEntity();
        entity.setId(role.getId().getValue());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        if (role.getAuditable() != null) {
            entity.setCreatedAt(role.getAuditable().getCreatedAt());
            entity.setUpdatedAt(role.getAuditable().getUpdatedAt());
            entity.setCreatedBy(role.getAuditable().getCreatedBy());
            entity.setUpdatedBy(role.getAuditable().getUpdatedBy());
        }
        return entity;
    }
}
