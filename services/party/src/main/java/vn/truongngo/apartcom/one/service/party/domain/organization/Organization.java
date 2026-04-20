package vn.truongngo.apartcom.one.service.party.domain.organization;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

@Getter
public class Organization extends AbstractAggregateRoot<PartyId> implements AggregateRoot<PartyId> {

    private final OrgType orgType;
    private String taxId;
    private String registrationNo;

    private Organization(PartyId id, OrgType orgType, String taxId, String registrationNo) {
        super(id);
        this.orgType        = orgType;
        this.taxId          = taxId;
        this.registrationNo = registrationNo;
    }

    public static Organization create(PartyId id, OrgType orgType,
                                      String taxId, String registrationNo) {
        return new Organization(id, orgType, taxId, registrationNo);
    }

    public static Organization reconstitute(PartyId id, OrgType orgType,
                                            String taxId, String registrationNo) {
        return new Organization(id, orgType, taxId, registrationNo);
    }

    /** orgType is immutable — only taxId and registrationNo can be updated. */
    public void updateInfo(String taxId, String registrationNo) {
        this.taxId          = taxId;
        this.registrationNo = registrationNo;
    }
}
