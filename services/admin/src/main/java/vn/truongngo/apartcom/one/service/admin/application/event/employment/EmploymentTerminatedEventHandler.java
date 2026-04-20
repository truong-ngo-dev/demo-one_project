package vn.truongngo.apartcom.one.service.admin.application.event.employment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReferenceRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContext;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContextStatus;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmploymentTerminatedEventHandler {

    private final UserRepository userRepository;
    private final BuildingReferenceRepository buildingReferenceRepository;

    @Transactional
    public void handle(EmploymentTerminatedPayload payload) {
        User user = userRepository.findByPartyId(payload.personId()).orElse(null);
        if (user == null) {
            log.warn("No user found for personId={} when processing employment termination", payload.personId());
            return;
        }

        List<RoleContext> operatorContexts = user.getRoleContexts().stream()
                .filter(ctx -> ctx.getScope() == Scope.OPERATOR
                        && ctx.getStatus() == RoleContextStatus.ACTIVE)
                .toList();

        boolean revoked = false;
        for (RoleContext ctx : operatorContexts) {
            boolean matches = buildingReferenceRepository.findById(ctx.getOrgId())
                    .map(ref -> payload.orgId().equals(ref.getManagingOrgId()))
                    .orElse(false);
            if (matches) {
                user.revokeRoleContext(Scope.OPERATOR, ctx.getOrgId());
                revoked = true;
            }
        }

        if (revoked) {
            userRepository.save(user);
        }
    }

    public record EmploymentTerminatedPayload(String employmentId, String personId, String orgId) {}
}
