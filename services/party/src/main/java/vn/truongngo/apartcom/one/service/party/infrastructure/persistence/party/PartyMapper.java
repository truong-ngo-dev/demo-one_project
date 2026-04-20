package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party;

import vn.truongngo.apartcom.one.service.party.domain.party.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PartyMapper {

    public static Party toDomain(PartyJpaEntity entity) {
        List<PartyIdentification> identifications = entity.getIdentifications().stream()
                .map(PartyMapper::identificationToDomain)
                .collect(Collectors.toList());
        return Party.reconstitute(
                PartyId.of(entity.getId()),
                entity.getType(),
                entity.getName(),
                entity.getStatus(),
                identifications,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PartyJpaEntity toEntity(Party party) {
        PartyJpaEntity entity = new PartyJpaEntity();
        entity.setId(party.getId().getValue());
        entity.setType(party.getType());
        entity.setName(party.getName());
        entity.setStatus(party.getStatus());
        entity.setCreatedAt(party.getCreatedAt());
        entity.setUpdatedAt(party.getUpdatedAt());

        List<PartyIdentificationJpaEntity> idEntities = party.getIdentifications().stream()
                .map(PartyMapper::identificationToEntity)
                .collect(Collectors.toList());
        entity.getIdentifications().clear();
        entity.getIdentifications().addAll(idEntities);

        return entity;
    }

    public static void updateEntity(PartyJpaEntity existing, Party party) {
        existing.setName(party.getName());
        existing.setStatus(party.getStatus());
        existing.setUpdatedAt(party.getUpdatedAt());

        existing.getIdentifications().clear();
        party.getIdentifications().stream()
                .map(PartyMapper::identificationToEntity)
                .forEach(existing.getIdentifications()::add);
    }

    private static PartyIdentification identificationToDomain(PartyIdentificationJpaEntity e) {
        return PartyIdentification.reconstitute(
                UUID.fromString(e.getId()),
                PartyId.of(e.getPartyId()),
                e.getType(),
                e.getValue(),
                e.getIssuedDate()
        );
    }

    private static PartyIdentificationJpaEntity identificationToEntity(PartyIdentification id) {
        PartyIdentificationJpaEntity e = new PartyIdentificationJpaEntity();
        e.setId(id.getId().toString());
        e.setPartyId(id.getPartyId().getValue());
        e.setType(id.getType());
        e.setValue(id.getValue());
        e.setIssuedDate(id.getIssuedDate());
        return e;
    }
}
