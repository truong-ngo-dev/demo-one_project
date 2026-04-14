package vn.truongngo.apartcom.one.service.admin.domain.abac.uielement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;

import java.util.List;
import java.util.Optional;

public interface UIElementRepository {

    UIElement save(UIElement element);

    Optional<UIElement> findById(Long id);

    Page<UIElement> findAll(Long resourceId, UIElementType type, String group, Scope scope, Pageable pageable);

    List<UIElement> findByElementIds(List<String> elementIds);

    void delete(Long id);

    boolean existsByElementId(String elementId);

    boolean existsByActionId(ActionId actionId);

    boolean existsByResourceId(ResourceId resourceId);
}
