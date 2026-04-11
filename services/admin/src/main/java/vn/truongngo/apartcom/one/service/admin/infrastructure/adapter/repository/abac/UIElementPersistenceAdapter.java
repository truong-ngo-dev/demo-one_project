package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElement;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementType;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.uielement.UIElementJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.uielement.UIElementJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.uielement.UIElementMapper;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UIElementPersistenceAdapter implements UIElementRepository {

    private final UIElementJpaRepository jpaRepository;

    @Override
    public UIElement save(UIElement element) {
        UIElementJpaEntity entity = UIElementMapper.toEntity(element);
        UIElementJpaEntity saved = jpaRepository.save(entity);
        return UIElementMapper.toDomain(saved);
    }

    @Override
    public Optional<UIElement> findById(Long id) {
        return jpaRepository.findById(id).map(UIElementMapper::toDomain);
    }

    @Override
    public Page<UIElement> findAll(Long resourceId, UIElementType type, String group, Pageable pageable) {
        return jpaRepository.search(resourceId, type, group, pageable)
                .map(UIElementMapper::toDomain);
    }

    @Override
    public List<UIElement> findByElementIds(List<String> elementIds) {
        return jpaRepository.findByElementIdIn(elementIds).stream()
                .map(UIElementMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByElementId(String elementId) {
        return jpaRepository.existsByElementId(elementId);
    }

    @Override
    public boolean existsByActionId(ActionId actionId) {
        return jpaRepository.existsByActionId(actionId.getValue());
    }

    @Override
    public boolean existsByResourceId(ResourceId resourceId) {
        return jpaRepository.existsByResourceId(resourceId.getValue());
    }
}
