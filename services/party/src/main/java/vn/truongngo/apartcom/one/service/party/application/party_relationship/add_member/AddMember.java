package vn.truongngo.apartcom.one.service.party.application.party_relationship.add_member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrganizationRepository;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrgType;
import vn.truongngo.apartcom.one.service.party.domain.party.*;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.*;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.event.MemberAddedEvent;

import java.time.LocalDate;

public class AddMember {

    public record Command(String personId, String groupId, PartyRoleType fromRole, LocalDate startDate) {
        public Command {
            Assert.hasText(personId, "personId is required");
            Assert.hasText(groupId, "groupId is required");
            Assert.notNull(fromRole, "fromRole is required");
            Assert.notNull(startDate, "startDate is required");
        }
    }

    public record Result(String relationshipId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final PartyRepository partyRepository;
        private final OrganizationRepository organizationRepository;
        private final PartyRelationshipRepository partyRelationshipRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            PartyId personId = PartyId.of(command.personId());
            PartyId groupId  = PartyId.of(command.groupId());

            Party fromParty = partyRepository.findById(personId)
                    .orElseThrow(PartyException::notFound);
            if (fromParty.getType() != PartyType.PERSON) {
                throw PartyRelationshipException.invalidFromPartyType();
            }

            Party toParty = partyRepository.findById(groupId)
                    .orElseThrow(PartyException::notFound);
            if (toParty.getType() != PartyType.HOUSEHOLD && toParty.getType() != PartyType.ORGANIZATION) {
                throw PartyRelationshipException.invalidToPartyType();
            }

            if (toParty.getType() == PartyType.ORGANIZATION) {
                OrgType orgType = organizationRepository.findById(groupId)
                        .orElseThrow(PartyException::organizationNotFound)
                        .getOrgType();
                if (orgType == OrgType.BQL) {
                    throw PartyRelationshipException.invalidToPartyType();
                }
            }

            if (partyRelationshipRepository.existsActiveByFromAndTo(personId, groupId,
                    PartyRelationshipType.MEMBER_OF)) {
                throw PartyRelationshipException.memberAlreadyInGroup();
            }

            PartyRelationship rel = PartyRelationship.create(
                    personId, groupId,
                    PartyRelationshipType.MEMBER_OF,
                    command.fromRole(), PartyRoleType.MEMBER,
                    command.startDate()
            );
            partyRelationshipRepository.save(rel);

            eventDispatcher.dispatch(new MemberAddedEvent(rel.getId(), personId, groupId, toParty.getType()));

            return new Result(rel.getId().getValue());
        }
    }
}
