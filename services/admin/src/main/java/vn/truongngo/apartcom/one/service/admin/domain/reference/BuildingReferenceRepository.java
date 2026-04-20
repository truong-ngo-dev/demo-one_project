package vn.truongngo.apartcom.one.service.admin.domain.reference;

import java.util.Optional;

public interface BuildingReferenceRepository {

    void upsert(BuildingReference ref);

    boolean existsById(String buildingId);

    Optional<BuildingReference> findById(String buildingId);
}
