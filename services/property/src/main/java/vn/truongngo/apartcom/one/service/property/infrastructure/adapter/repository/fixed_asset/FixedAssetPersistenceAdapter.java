package vn.truongngo.apartcom.one.service.property.infrastructure.adapter.repository.fixed_asset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAsset;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetId;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetRepository;
import vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset.FixedAssetJpaEntity;
import vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset.FixedAssetJpaRepository;
import vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset.FixedAssetMapper;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FixedAssetPersistenceAdapter implements FixedAssetRepository {

    private final FixedAssetJpaRepository jpaRepository;

    @Override
    public Optional<FixedAsset> findById(FixedAssetId id) {
        return jpaRepository.findById(id.getValue()).map(FixedAssetMapper::toDomain);
    }

    @Override
    public void save(FixedAsset asset) {
        Optional<FixedAssetJpaEntity> existing = jpaRepository.findById(asset.getId().getValue());
        if (existing.isPresent()) {
            FixedAssetMapper.updateEntity(existing.get(), asset);
            jpaRepository.save(existing.get());
        } else {
            jpaRepository.save(FixedAssetMapper.toEntity(asset));
        }
    }

    @Override
    public void delete(FixedAssetId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public List<FixedAsset> findByPathPrefix(String pathPrefix) {
        return jpaRepository.findAllByPathStartingWith(pathPrefix)
                .stream()
                .map(FixedAssetMapper::toDomain)
                .toList();
    }
}
