package vn.truongngo.apartcom.one.service.oauth2.infrastructure.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.truongngo.apartcom.one.service.oauth2.domain.user.User;
import vn.truongngo.apartcom.one.service.oauth2.domain.user.UserIdentityService;

/**
 * UserDetailsService cho Spring Security form login.
 * Resolves User via UserIdentityService (ACL over admin-service).
 * Principal name = userId → sub claim trong JWT sẽ = userId.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserDetailsService implements UserDetailsService {

    private final UserIdentityService userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        if (!StringUtils.hasText(usernameOrEmail)) {
            throw new UsernameNotFoundException("Username or email is missing");
        }

        try {
            User user = userRepository.findByCredentials(usernameOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));

            return mapToUserDetails(user);
        } catch (UsernameNotFoundException e) {
            log.warn("[loadUserByUsername] User not found: '{}'", usernameOrEmail);
            throw e;
        } catch (Exception e) {
            log.error("[loadUserByUsername] Cannot retrieve user '{}': {}", usernameOrEmail, e.getMessage());
            throw new UsernameNotFoundException("Cannot retrieve user: " + usernameOrEmail, e);
        }
    }

    private static UserDetails mapToUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getId().getValueAsString(),   // principal name = userId → sub claim
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                user.isActive(),                   // enabled
                true,                              // accountNonExpired
                true,                              // credentialsNonExpired
                !user.isLocked(),                  // accountNonLocked
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList()
        );
    }
}
