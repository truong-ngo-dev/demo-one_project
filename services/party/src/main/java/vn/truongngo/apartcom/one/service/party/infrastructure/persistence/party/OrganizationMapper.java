package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party;

import vn.truongngo.apartcom.one.service.party.domain.organization.Organization;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

public class OrganizationMapper {

    public static Organization toDomain(OrganizationJpaEntity entity) {
        return Organization.reconstitute(
                PartyId.of(entity.getPartyId()),
                entity.getOrgType(),
                entity.getTaxId(),
                entity.getRegistrationNo()
        );
    }

    public static OrganizationJpaEntity toEntity(Organization org) {
        OrganizationJpaEntity entity = new OrganizationJpaEntity();
        entity.setPartyId(org.getId().getValue());
        entity.setOrgType(org.getOrgType());
        entity.setTaxId(org.getTaxId());
        entity.setRegistrationNo(org.getRegistrationNo());
        return entity;
    }

    public static void updateEntity(OrganizationJpaEntity existing, Organization org) {
        existing.setTaxId(org.getTaxId());
        existing.setRegistrationNo(org.getRegistrationNo());
    }
}
