package vn.truongngo.apartcom.one.service.party.application.party_relationship.remove_member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.*;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.event.MemberRemovedEvent;

import java.time.LocalDate;

public class RemoveMember {

    public record Command(String relationshipId) {
        public Command {
            Assert.hasText(relationshipId, "relationshipId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final PartyRelationshipRepository partyRelationshipRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Void handle(Command command) {
            PartyRelationship rel = partyRelationshipRepository
                    .findById(PartyRelationshipId.of(command.relationshipId()))
                    .orElseThrow(PartyRelationshipException::notFound);

            rel.end(LocalDate.now());
            partyRelationshipRepository.save(rel);

            eventDispatcher.dispatch(new MemberRemovedEvent(rel.getId(), rel.getFromPartyId(), rel.getToPartyId()));

            return null;
        }
    }
}
