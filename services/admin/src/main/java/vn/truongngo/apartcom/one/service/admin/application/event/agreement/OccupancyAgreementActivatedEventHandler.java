package vn.truongngo.apartcom.one.service.admin.application.event.agreement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.party.PartyClient;
import vn.truongngo.apartcom.one.service.admin.domain.user.OrgType;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OccupancyAgreementActivatedEventHandler {

    private final UserRepository userRepository;
    private final PartyClient partyClient;

    @Transactional
    public void handle(OccupancyAgreementActivatedPayload payload) {
        String partyType     = payload.partyType();
        String agreementType = payload.agreementType();

        if (("OWNERSHIP".equals(agreementType) || "LEASE".equals(agreementType))
                && "PERSON".equals(partyType)) {
            addResidentContext(payload.partyId(), payload.assetId());

        } else if ("LEASE".equals(agreementType) && "HOUSEHOLD".equals(partyType)) {
            List<String> personIds = partyClient.getMembers(payload.partyId());
            for (String personId : personIds) {
                addResidentContext(personId, payload.assetId());
            }

        } else if ("LEASE".equals(agreementType) && "ORGANIZATION".equals(partyType)) {
            List<String> personIds = partyClient.getMembers(payload.partyId());
            if (!personIds.isEmpty()) {
                // first member is TENANT_ADMIN
                addTenantContext(personIds.get(0), payload.partyId());
            }
        }
    }

    private void addResidentContext(String partyId, String assetId) {
        Optional<User> userOpt = userRepository.findByPartyId(partyId);
        if (userOpt.isEmpty()) {
            log.warn("No user found for partyId={} when activating RESIDENT context for asset={}", partyId, assetId);
            return;
        }
        User user = userOpt.get();
        user.addRoleContext(Scope.RESIDENT, assetId, OrgType.FIXED_ASSET, Set.of());
        userRepository.save(user);
    }

    private void addTenantContext(String partyId, String orgId) {
        Optional<User> userOpt = userRepository.findByPartyId(partyId);
        if (userOpt.isEmpty()) {
            log.warn("No user found for partyId={} when activating TENANT context for org={}", partyId, orgId);
            return;
        }
        User user = userOpt.get();
        user.addRoleContext(Scope.TENANT, orgId, OrgType.PARTY, Set.of());
        userRepository.save(user);
    }

    public record OccupancyAgreementActivatedPayload(
            String agreementId,
            String partyId,
            String partyType,
            String assetId,
            String agreementType
    ) {}
}
