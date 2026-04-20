package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrgType;

@Entity
@Table(name = "organization")
@Getter @Setter @NoArgsConstructor
public class OrganizationJpaEntity {

    @Id
    @Column(name = "party_id", nullable = false, length = 36)
    private String partyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_type", nullable = false)
    private OrgType orgType;

    @Column(name = "tax_id", length = 20)
    private String taxId;

    @Column(name = "registration_no", length = 50)
    private String registrationNo;
}
