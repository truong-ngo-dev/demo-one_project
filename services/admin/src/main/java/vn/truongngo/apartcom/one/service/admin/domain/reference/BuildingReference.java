package vn.truongngo.apartcom.one.service.admin.domain.reference;

import lombok.Getter;

import java.time.Instant;

@Getter
public class BuildingReference {

    private final String buildingId;
    private final String name;
    private final String managingOrgId;  // nullable
    private final Instant cachedAt;

    private BuildingReference(String buildingId, String name, String managingOrgId, Instant cachedAt) {
        this.buildingId     = buildingId;
        this.name           = name;
        this.managingOrgId  = managingOrgId;
        this.cachedAt       = cachedAt;
    }

    public static BuildingReference of(String buildingId, String name, String managingOrgId) {
        return new BuildingReference(buildingId, name, managingOrgId, Instant.now());
    }

    public static BuildingReference reconstitute(String buildingId, String name,
                                                 String managingOrgId, Instant cachedAt) {
        return new BuildingReference(buildingId, name, managingOrgId, cachedAt);
    }
}
