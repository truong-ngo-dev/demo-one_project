package vn.truongngo.apartcom.one.service.admin.domain.reference;

import lombok.Getter;

import java.time.Instant;

@Getter
public class OrgReference {

    private final String orgId;
    private final String name;
    private final String orgType;
    private final Instant cachedAt;

    private OrgReference(String orgId, String name, String orgType, Instant cachedAt) {
        this.orgId    = orgId;
        this.name     = name;
        this.orgType  = orgType;
        this.cachedAt = cachedAt;
    }

    public static OrgReference of(String orgId, String name, String orgType) {
        return new OrgReference(orgId, name, orgType, Instant.now());
    }

    public static OrgReference reconstitute(String orgId, String name, String orgType, Instant cachedAt) {
        return new OrgReference(orgId, name, orgType, cachedAt);
    }
}
