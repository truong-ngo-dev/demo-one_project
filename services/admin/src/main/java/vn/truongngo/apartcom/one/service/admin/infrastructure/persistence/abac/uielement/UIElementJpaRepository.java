package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.uielement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementType;

import java.util.List;

public interface UIElementJpaRepository extends JpaRepository<UIElementJpaEntity, Long> {

    boolean existsByElementId(String elementId);

    boolean existsByActionId(Long actionId);

    boolean existsByResourceId(Long resourceId);

    List<UIElementJpaEntity> findByElementIdIn(List<String> elementIds);

    @Query("SELECT e FROM UIElementJpaEntity e WHERE " +
           "(:resourceId IS NULL OR e.resourceId = :resourceId) AND " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:elementGroup IS NULL OR e.elementGroup = :elementGroup)")
    Page<UIElementJpaEntity> search(
            @Param("resourceId") Long resourceId,
            @Param("type") UIElementType type,
            @Param("elementGroup") String elementGroup,
            Pageable pageable);
}
