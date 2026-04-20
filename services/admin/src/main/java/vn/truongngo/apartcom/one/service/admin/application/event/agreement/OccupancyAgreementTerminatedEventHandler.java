package vn.truongngo.apartcom.one.service.admin.application.event.agreement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignmentRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OccupancyAgreementTerminatedEventHandler {

    private final UserRepository userRepository;
    private final Optional<TenantSubRoleAssignmentRepository> tenantSubRoleRepository;

    @Transactional
    public void handle(OccupancyAgreementTerminatedPayload payload) {
        String partyType     = payload.partyType();
        String agreementType = payload.agreementType();

        if ("OWNERSHIP".equals(agreementType)
                || ("LEASE".equals(agreementType)
                    && ("PERSON".equals(partyType) || "HOUSEHOLD".equals(partyType)))) {

            List<User> users = userRepository.findAllByActiveRoleContext(Scope.RESIDENT, payload.assetId());
            for (User user : users) {
                user.revokeRoleContext(Scope.RESIDENT, payload.assetId());
                userRepository.save(user);
            }

        } else if ("LEASE".equals(agreementType) && "ORGANIZATION".equals(partyType)) {

            List<User> users = userRepository.findAllByActiveRoleContext(Scope.TENANT, payload.partyId());
            for (User user : users) {
                user.revokeRoleContext(Scope.TENANT, payload.partyId());
                userRepository.save(user);
            }

            // TODO: delete TenantSubRoleAssignments for this org (Phase 2 dependency)
            tenantSubRoleRepository.ifPresent(repo -> repo.deleteAllByOrgId(payload.partyId()));
        }
    }

    public record OccupancyAgreementTerminatedPayload(
            String agreementId,
            String partyId,
            String partyType,
            String assetId,
            String agreementType
    ) {}
}
