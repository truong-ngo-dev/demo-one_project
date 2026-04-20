package vn.truongngo.apartcom.one.service.party.domain.party;

import lombok.Getter;


import java.time.LocalDate;
import java.util.UUID;

@Getter
public class PartyIdentification {

    private final UUID id;
    private final PartyId partyId;
    private final PartyIdentificationType type;
    private final String value;
    private final LocalDate issuedDate;

    private PartyIdentification(UUID id, PartyId partyId, PartyIdentificationType type,
                                String value, LocalDate issuedDate) {
        this.id         = id;
        this.partyId    = partyId;
        this.type       = type;
        this.value      = value;
        this.issuedDate = issuedDate;
    }

    public static PartyIdentification create(PartyId partyId, PartyIdentificationType type,
                                             String value, LocalDate issuedDate) {
        return new PartyIdentification(UUID.randomUUID(), partyId, type, value, issuedDate);
    }

    public static PartyIdentification reconstitute(UUID id, PartyId partyId,
                                                   PartyIdentificationType type,
                                                   String value, LocalDate issuedDate) {
        return new PartyIdentification(id, partyId, type, value, issuedDate);
    }
}
