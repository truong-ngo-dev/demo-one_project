package vn.truongngo.apartcom.one.service.admin.application.auth.get_contexts;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReferenceRepository;
import vn.truongngo.apartcom.one.service.admin.domain.reference.OrgReferenceRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.OrgType;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContext;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContextStatus;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.List;

public class GetUserContexts {

    public record Query(String userId) {
        public Query {
            Assert.hasText(userId, "userId is required");
        }
    }

    public record ContextView(
            Long contextId,
            Scope scope,
            String orgId,
            OrgType orgType,
            String displayName,
            List<String> roleIds
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<ContextView>> {

        private final UserRepository userRepository;
        private final BuildingReferenceRepository buildingReferenceRepository;
        private final OrgReferenceRepository orgReferenceRepository;

        @Override
        public List<ContextView> handle(Query query) {
            User user = userRepository.findById(UserId.of(query.userId()))
                    .orElseThrow(UserException::notFound);

            return user.getRoleContexts().stream()
                    .filter(ctx -> ctx.getStatus() == RoleContextStatus.ACTIVE)
                    .map(ctx -> toContextView(ctx))
                    .toList();
        }

        private ContextView toContextView(RoleContext ctx) {
            String displayName = resolveDisplayName(ctx);
            List<String> roleIds = ctx.getRoleIds().stream()
                    .map(r -> r.getValue())
                    .toList();
            return new ContextView(ctx.getId(), ctx.getScope(), ctx.getOrgId(),
                    ctx.getOrgType(), displayName, roleIds);
        }

        private String resolveDisplayName(RoleContext ctx) {
            if (ctx.getScope() == Scope.ADMIN) {
                return "Admin Portal";
            }
            if (ctx.getScope() == Scope.OPERATOR || ctx.getScope() == Scope.RESIDENT) {
                return buildingReferenceRepository.findById(ctx.getOrgId())
                        .map(ref -> ref.getName())
                        .orElse(ctx.getOrgId());
            }
            if (ctx.getScope() == Scope.TENANT) {
                return orgReferenceRepository.findById(ctx.getOrgId())
                        .map(ref -> ref.getName())
                        .orElse(ctx.getOrgId());
            }
            return ctx.getOrgId();
        }
    }
}
