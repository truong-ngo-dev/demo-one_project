package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.reference;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingReferenceJpaRepository
        extends JpaRepository<BuildingReferenceJpaEntity, String> {
}
