package vn.truongngo.apartcom.one.service.admin.application.operator.find_operators_by_building;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContext;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContextStatus;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.List;

public class FindOperatorsByBuilding {

    public record Query(String buildingId) {
        public Query {
            Assert.hasText(buildingId, "buildingId is required");
        }
    }

    public record OperatorView(
            String userId,
            String partyId,
            String buildingId,
            List<String> roleIds,
            RoleContextStatus status
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<OperatorView>> {

        private final UserRepository userRepository;

        @Override
        public List<OperatorView> handle(Query query) {
            return userRepository.findAllByActiveRoleContext(Scope.OPERATOR, query.buildingId())
                    .stream()
                    .map(user -> {
                        RoleContext ctx = user.getRoleContexts().stream()
                                .filter(c -> c.matchesScope(Scope.OPERATOR, query.buildingId()))
                                .findFirst()
                                .orElseThrow();
                        List<String> roleIds = ctx.getRoleIds().stream()
                                .map(r -> r.getValue())
                                .toList();
                        return new OperatorView(
                                user.getId().getValue(),
                                user.getPartyId(),
                                query.buildingId(),
                                roleIds,
                                ctx.getStatus()
                        );
                    })
                    .toList();
        }
    }
}
