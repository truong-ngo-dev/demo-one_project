package vn.truongngo.apartcom.one.service.admin.application.operator.assign_operator_context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReferenceRepository;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleException;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.OrgType;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserErrorCode;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AssignOperatorContext {

    public record Command(String userId, String buildingId, List<String> roleIds) {
        public Command {
            Assert.hasText(userId, "userId is required");
            Assert.hasText(buildingId, "buildingId is required");
            Assert.notNull(roleIds, "roleIds is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final BuildingReferenceRepository buildingReferenceRepository;

        @Override
        @Transactional
        public Void handle(Command command) {
            User user = userRepository.findById(UserId.of(command.userId()))
                    .orElseThrow(UserException::notFound);

            // B2: partyId must be set
            if (user.getPartyId() == null) {
                throw new DomainException(UserErrorCode.PARTY_ID_REQUIRED);
            }

            // B3: building must exist in reference cache
            if (!buildingReferenceRepository.existsById(command.buildingId())) {
                throw new DomainException(UserErrorCode.BUILDING_NOT_FOUND);
            }

            Set<RoleId> roleIdSet = command.roleIds().stream()
                    .map(RoleId::of)
                    .collect(Collectors.toSet());

            // B6: all roles must exist and have scope == OPERATOR
            var roles = roleRepository.findAllByIds(roleIdSet);
            if (roles.size() != roleIdSet.size()) {
                throw RoleException.notFound();
            }
            roles.forEach(role -> {
                if (role.getScope() != Scope.OPERATOR) {
                    throw new DomainException(UserErrorCode.ROLE_SCOPE_MISMATCH);
                }
            });

            user.addRoleContext(Scope.OPERATOR, command.buildingId(), OrgType.FIXED_ASSET, roleIdSet);
            userRepository.save(user);

            return null;
        }
    }
}
