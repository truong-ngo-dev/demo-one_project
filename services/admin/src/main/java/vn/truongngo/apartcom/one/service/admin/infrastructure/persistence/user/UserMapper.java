package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.user.SocialConnection;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserPassword;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role.RoleJpaEntity;
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

        Set<RoleId> roleIds = entity.getRoles()
                .stream()
                .map(r -> RoleId.of(r.getId()))
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
                roleIds,
                entity.isUsernameChanged(),
                entity.getLockedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // Update scalar fields and roles of an existing JpaEntity (no social connections)
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

        Set<String> roleIds = user.getRoleIds().stream()
                .map(RoleId::getValue)
                .collect(Collectors.toSet());
        existing.setRoles(new HashSet<>(roleJpaRepository.findAllById(roleIds)));
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

        Set<String> roleIds = user.getRoleIds().stream()
                .map(RoleId::getValue)
                .collect(Collectors.toSet());
        Set<RoleJpaEntity> roleEntities = new HashSet<>(roleJpaRepository.findAllById(roleIds));
        entity.setRoles(roleEntities);

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
        return entity;
    }
}