package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceDefinitionJpaRepository extends JpaRepository<ResourceDefinitionJpaEntity, Long> {

    boolean existsByName(String name);

    java.util.Optional<ResourceDefinitionJpaEntity> findByName(String name);

    @Query("SELECT r FROM ResourceDefinitionJpaEntity r WHERE " +
           ":keyword IS NULL OR " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ResourceDefinitionJpaEntity> search(@Param("keyword") String keyword, Pageable pageable);
}
