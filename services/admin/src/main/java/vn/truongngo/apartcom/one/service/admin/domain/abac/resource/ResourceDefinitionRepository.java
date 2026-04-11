package vn.truongngo.apartcom.one.service.admin.domain.abac.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ResourceDefinitionRepository {

    /**
     * Persists the resource (create or update) and returns the saved instance with
     * the database-assigned id populated. Required because id is BIGINT auto-increment.
     */
    ResourceDefinition save(ResourceDefinition resource);

    Optional<ResourceDefinition> findById(ResourceId id);

    Page<ResourceDefinition> findAll(String keyword, Pageable pageable);

    void delete(ResourceId id);

    boolean existsByName(String name);

    java.util.Optional<ResourceDefinition> findByName(String name);

    /** Guard for delete — returns true if any Policy references this resource. */
    boolean existsByIdWithPolicyRef(ResourceId id);

    /** Guard for delete — returns true if any UIElement references this resource. */
    boolean existsByIdWithUIElementRef(ResourceId id);
}
