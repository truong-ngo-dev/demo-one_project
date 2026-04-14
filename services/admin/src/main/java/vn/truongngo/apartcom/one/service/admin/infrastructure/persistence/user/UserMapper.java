package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContext;
import vn.truongngo.apartcom.one.service.admin.domain.user.SocialConnection;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserPassword;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role.RoleJpaRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleJpaRepository roleJpaRepository;

    // JpaEntity → Domain
    public User toDomain(UserJpaEntity entity) {
        UserPassword password = entity.getHashedPassword() != null
                ? UserPassword.ofHashed(entity.getHashedPassword())
                : null;

        Set<SocialConnection> socialConnections = entity.getSocialConnections()
                .stream()
                .map(s -> SocialConnection.reconstitute(s.getProvider(), s.getSocialId(), s.getEmail(), s.getConnectedAt()))
                .collect(Collectors.toSet());

        Set<RoleContext> roleContexts = entity.getRoleContexts()
                .stream()
                .map(ctx -> {
                    Set<RoleId> ctxRoleIds = ctx.getRoles().stream()
                            .map(r -> RoleId.of(r.getId()))
                            .collect(Collectors.toSet());
                    // '' sentinel → null domain orgId
                    String orgId = ctx.getOrgId().isEmpty() ? null : ctx.getOrgId();
                    return RoleContext.reconstitute(ctx.getId(), ctx.getScope(), orgId, ctxRoleIds);
                })
                .collect(Collectors.toSet());

        return User.reconstitute(
                UserId.of(entity.getId()),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getFullName(),
                password,
                socialConnections,
                entity.getStatus(),
                roleContexts,
                entity.isUsernameChanged(),
                entity.getLockedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // Update scalar fields and roleContexts of an existing JpaEntity (no social connections)
    public void updateFields(UserJpaEntity existing, User user) {
        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setPhoneNumber(user.getPhoneNumber());
        existing.setFullName(user.getFullName());
        existing.setHashedPassword(user.getPassword() != null ? user.getPassword().getHashedValue() : null);
        existing.setStatus(user.getStatus());
        existing.setUsernameChanged(user.isUsernameChanged());
        existing.setLockedAt(user.getLockedAt());
        existing.setUpdatedAt(user.getUpdatedAt());

        existing.getRoleContexts().clear();
        for (RoleContext ctx : user.getRoleContexts()) {
            existing.getRoleContexts().add(buildContextEntity(user.getId().getValue(), ctx));
        }
    }

    // Domain → JpaEntity (insert)
    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId().getValue());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setFullName(user.getFullName());
        entity.setHashedPassword(
                user.getPassword() != null
                        ? user.getPassword().getHashedValue()
                        : null
        );
        entity.setStatus(user.getStatus());
        entity.setUsernameChanged(user.isUsernameChanged());
        entity.setLockedAt(user.getLockedAt());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());

        Set<SocialConnectionJpaEntity> socialEntities = user.getSocialConnections()
                .stream()
                .map(s -> {
                    SocialConnectionJpaEntity se = new SocialConnectionJpaEntity();
                    se.setUserId(user.getId().getValue());
                    se.setProvider(s.getProvider());
                    se.setSocialId(s.getSocialId());
                    se.setEmail(s.getEmail());
                    se.setConnectedAt(s.getConnectedAt());
                    return se;
                })
                .collect(Collectors.toSet());
        entity.setSocialConnections(socialEntities);

        Set<UserRoleContextJpaEntity> contextEntities = user.getRoleContexts().stream()
                .map(ctx -> buildContextEntity(user.getId().getValue(), ctx))
                .collect(Collectors.toSet());
        entity.setRoleContexts(contextEntities);

        return entity;
    }

    private UserRoleContextJpaEntity buildContextEntity(String userId, RoleContext ctx) {
        UserRoleContextJpaEntity ctxEntity = new UserRoleContextJpaEntity();
        ctxEntity.setUserId(userId);
        ctxEntity.setScope(ctx.getScope());
        // null domain orgId → '' sentinel in DB (for UNIQUE constraint compatibility)
        ctxEntity.setOrgId(ctx.getOrgId() != null ? ctx.getOrgId() : "");
        Set<String> ctxRoleIds = ctx.getRoleIds().stream()
                .map(RoleId::getValue)
                .collect(Collectors.toSet());
        ctxEntity.setRoles(new HashSet<>(roleJpaRepository.findAllById(ctxRoleIds)));
        return ctxEntity;
    }
}
