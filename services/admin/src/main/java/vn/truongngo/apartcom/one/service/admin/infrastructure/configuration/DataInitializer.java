package vn.truongngo.apartcom.one.service.admin.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role.RoleJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role.RoleJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user.UserJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user.UserJpaRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Seeds initial data on startup:
 * - ADMIN role
 * - admin@example.com / admin123 user with ADMIN role
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_ROLE_ID   = "00000000-0000-0000-0000-000000000001";
    private static final String ADMIN_ROLE_NAME = "ADMIN";
    private static final String ADMIN_USER_ID   = "00000000-0000-0000-0000-000000000002";
    private static final String ADMIN_EMAIL     = "admin@example.com";
    private static final String ADMIN_USERNAME  = "admin";
    private static final String ADMIN_PASSWORD  = "admin123";

    private final RoleJpaRepository roleJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        RoleJpaEntity adminRole = seedAdminRole();
        seedAdminUser(adminRole);
    }

    private RoleJpaEntity seedAdminRole() {
        return roleJpaRepository.findByName(ADMIN_ROLE_NAME).orElseGet(() -> {
            RoleJpaEntity role = new RoleJpaEntity();
            role.setId(ADMIN_ROLE_ID);
            role.setName(ADMIN_ROLE_NAME);
            role.setDescription("System administrator");
            role.setCreatedAt(Instant.now().toEpochMilli());
            log.info("[DataInitializer] Creating ADMIN role");
            return roleJpaRepository.save(role);
        });
    }

    private void seedAdminUser(RoleJpaEntity adminRole) {
        if (userJpaRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }
        UserJpaEntity user = new UserJpaEntity();
        user.setId(ADMIN_USER_ID);
        user.setUsername(ADMIN_USERNAME);
        user.setEmail(ADMIN_EMAIL);
        user.setFullName("Administrator");
        user.setHashedPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setRoles(Set.of(adminRole));
        log.info("[DataInitializer] Creating admin user: {}", ADMIN_EMAIL);
        userJpaRepository.save(user);
    }
}
