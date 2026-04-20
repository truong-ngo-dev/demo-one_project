package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContextStatus;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserStatus;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user.SocialConnectionJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user.UserJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user.UserJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user.UserMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.getValue())
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return jpaRepository.findByPhoneNumber(phoneNumber)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findBySocialConnection(String provider, String socialId) {
        return jpaRepository.findBySocialConnection(provider, socialId)
                .map(userMapper::toDomain);
    }

    @Override
    public void save(User user) {
        Optional<UserJpaEntity> existingOpt = jpaRepository.findById(user.getId().getValue());
        if (existingOpt.isEmpty()) {
            jpaRepository.save(userMapper.toEntity(user));
            return;
        }

        UserJpaEntity existing = existingOpt.get();
        userMapper.updateFields(existing, user);

        Set<String> existingProviders = existing.getSocialConnections().stream()
                .map(SocialConnectionJpaEntity::getProvider)
                .collect(Collectors.toSet());

        user.getSocialConnections().stream()
                .filter(sc -> !existingProviders.contains(sc.getProvider()))
                .forEach(sc -> {
                    SocialConnectionJpaEntity se = new SocialConnectionJpaEntity();
                    se.setUserId(user.getId().getValue());
                    se.setProvider(sc.getProvider());
                    se.setSocialId(sc.getSocialId());
                    se.setEmail(sc.getEmail());
                    se.setConnectedAt(sc.getConnectedAt());
                    existing.getSocialConnections().add(se);
                });

        jpaRepository.save(existing);
    }

    @Override
    public void delete(UserId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return jpaRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean existsByRoleId(RoleId roleId) {
        return jpaRepository.existsUserWithRole(roleId.getValue());
    }

    @Override
    public Page<User> findAll(String keyword, UserStatus status, RoleId roleId, Pageable pageable) {
        String roleIdValue = roleId != null ? roleId.getValue() : null;
        return jpaRepository.searchUsers(keyword, status, roleIdValue, pageable)
                .map(userMapper::toDomain);
    }

    @Override
    public long countByRoleName(String roleName) {
        return jpaRepository.countByRoleName(roleName);
    }

    @Override
    public Optional<User> findByPartyId(String partyId) {
        return jpaRepository.findByPartyId(partyId).map(userMapper::toDomain);
    }

    @Override
    public List<User> findAllByActiveRoleContext(Scope scope, String orgId) {
        return jpaRepository.findAllByActiveRoleContext(scope, orgId, RoleContextStatus.ACTIVE)
                .stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }
}
