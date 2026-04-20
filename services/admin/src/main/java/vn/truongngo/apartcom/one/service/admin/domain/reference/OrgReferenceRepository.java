package vn.truongngo.apartcom.one.service.admin.domain.reference;

import java.util.Optional;

public interface OrgReferenceRepository {

    void upsert(OrgReference ref);

    boolean existsById(String orgId);

    Optional<OrgReference> findById(String orgId);
}
