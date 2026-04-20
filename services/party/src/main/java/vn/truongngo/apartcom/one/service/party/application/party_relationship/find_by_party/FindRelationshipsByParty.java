package vn.truongngo.apartcom.one.service.party.application.party_relationship.find_by_party;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FindRelationshipsByParty {

    public record Query(String partyId, String direction) {
        public Query {
            Assert.hasText(partyId, "partyId is required");
            Assert.hasText(direction, "direction is required");
        }
    }

    public record RelationshipView(
            String id,
            String fromPartyId,
            String toPartyId,
            PartyRelationshipType type,
            PartyRoleType fromRole,
            PartyRoleType toRole,
            PartyRelationshipStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    static class Mapper {
        static RelationshipView toView(PartyRelationship rel) {
            return new RelationshipView(
                    rel.getId().getValue(),
                    rel.getFromPartyId().getValue(),
                    rel.getToPartyId().getValue(),
                    rel.getType(),
                    rel.getFromRole(),
                    rel.getToRole(),
                    rel.getStatus(),
                    rel.getStartDate(),
                    rel.getEndDate()
            );
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<RelationshipView>> {

        private final PartyRelationshipRepository partyRelationshipRepository;

        @Override
        public List<RelationshipView> handle(Query query) {
            PartyId partyId = PartyId.of(query.partyId());

            List<PartyRelationship> results = switch (query.direction().toUpperCase()) {
                case "FROM" -> partyRelationshipRepository.findByFromPartyId(partyId);
                case "TO"   -> partyRelationshipRepository.findByToPartyId(partyId);
                default     -> merge(
                        partyRelationshipRepository.findByFromPartyId(partyId),
                        partyRelationshipRepository.findByToPartyId(partyId)
                );
            };

            return results.stream().map(Mapper::toView).toList();
        }

        private List<PartyRelationship> merge(List<PartyRelationship> from, List<PartyRelationship> to) {
            Map<String, PartyRelationship> deduped = new LinkedHashMap<>();
            from.forEach(r -> deduped.put(r.getId().getValue(), r));
            to.forEach(r -> deduped.put(r.getId().getValue(), r));
            return new ArrayList<>(deduped.values());
        }
    }
}
