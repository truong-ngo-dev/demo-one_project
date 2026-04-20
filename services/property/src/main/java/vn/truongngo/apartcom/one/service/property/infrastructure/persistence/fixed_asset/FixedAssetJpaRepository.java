package vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixedAssetJpaRepository extends JpaRepository<FixedAssetJpaEntity, String> {

    List<FixedAssetJpaEntity> findAllByPathStartingWith(String pathPrefix);

    List<FixedAssetJpaEntity> findAllByParentId(String parentId);
}
