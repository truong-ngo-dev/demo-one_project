package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.resource.ResourceDefinitionJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.resource.ResourceDefinitionJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.resource.ResourceDefinitionMapper;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.uielement.UIElementJpaRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResourceDefinitionPersistenceAdapter implements ResourceDefinitionRepository {

    private final ResourceDefinitionJpaRepository jpaRepository;
    private final UIElementJpaRepository uiElementJpaRepository;

    @Override
    public ResourceDefinition save(ResourceDefinition resource) {
        ResourceDefinitionJpaEntity entity = ResourceDefinitionMapper.toEntity(resource);
        ResourceDefinitionJpaEntity saved = jpaRepository.save(entity);
        return ResourceDefinitionMapper.toDomain(saved);
    }

    @Override
    public Optional<ResourceDefinition> findById(ResourceId id) {
        return jpaRepository.findById(id.getValue())
                .map(ResourceDefinitionMapper::toDomain);
    }

    @Override
    public Page<ResourceDefinition> findAll(String keyword, Pageable pageable) {
        return jpaRepository.search(keyword, pageable)
                .map(ResourceDefinitionMapper::toDomain);
    }

    @Override
    public void delete(ResourceId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public Optional<ResourceDefinition> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(ResourceDefinitionMapper::toDomain);
    }

    @Override
    public boolean existsByIdWithPolicyRef(ResourceId id) {
        // TODO: implement Batch 2 — query policy table for resource references
        return false;
    }

    @Override
    public boolean existsByIdWithUIElementRef(ResourceId id) {
        return uiElementJpaRepository.existsByResourceId(id.getValue());
    }
}
