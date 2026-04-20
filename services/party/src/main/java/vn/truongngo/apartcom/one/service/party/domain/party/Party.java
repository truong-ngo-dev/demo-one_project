package vn.truongngo.apartcom.one.service.party.domain.party;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class Party extends AbstractAggregateRoot<PartyId> implements AggregateRoot<PartyId> {

    private final PartyType type;
    private String name;
    private PartyStatus status;
    private final List<PartyIdentification> identifications;
    private final Instant createdAt;
    private Instant updatedAt;

    private Party(PartyId id, PartyType type, String name, PartyStatus status,
                  List<PartyIdentification> identifications, Instant createdAt, Instant updatedAt) {
        super(id);
        this.type            = type;
        this.name            = name;
        this.status          = status;
        this.identifications = new ArrayList<>(identifications);
        this.createdAt       = createdAt;
        this.updatedAt       = updatedAt;
    }

    public static Party create(PartyType type, String name) {
        Assert.hasText(name, "Party name is required");
        Instant now = Instant.now();
        return new Party(PartyId.generate(), type, name, PartyStatus.ACTIVE,
                         new ArrayList<>(), now, now);
    }

    public static Party reconstitute(PartyId id, PartyType type, String name, PartyStatus status,
                                     List<PartyIdentification> identifications,
                                     Instant createdAt, Instant updatedAt) {
        return new Party(id, type, name, status, identifications, createdAt, updatedAt);
    }

    public List<PartyIdentification> getIdentifications() {
        return Collections.unmodifiableList(identifications);
    }

    // ── Behaviors ────────────────────────────────────────────────────────────

    public void addIdentification(PartyIdentificationType type, String value, LocalDate issuedDate) {
        boolean duplicate = identifications.stream()
                .anyMatch(i -> i.getType() == type && i.getValue().equals(value));
        if (duplicate) throw PartyException.identificationAlreadyExists();
        identifications.add(PartyIdentification.create(this.getId(), type, value, issuedDate));
        this.updatedAt = Instant.now();
    }

    public void removeIdentification(UUID identificationId) {
        boolean removed = identifications.removeIf(i -> i.getId().equals(identificationId));
        if (!removed) throw PartyException.identificationNotFound();
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (this.status == PartyStatus.INACTIVE) throw PartyException.alreadyInactive();
        this.status    = PartyStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }
}
