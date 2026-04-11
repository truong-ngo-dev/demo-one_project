package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.abac.context.Subject;
import vn.truongngo.apartcom.one.lib.abac.pip.SubjectProvider;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

/**
 * Builds a Subject from the JWT principal.
 * JWT sub = userId (string). Loads user roles from UserRepository + RoleRepository.
 */
@Component
@RequiredArgsConstructor
public class AdminSubjectProvider implements SubjectProvider {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public Subject getSubject(Principal principal) {
        Subject subject = new Subject();
        subject.setAttributes(new HashMap<>());

        if (principal == null) {
            subject.setRoles(List.of());
            return subject;
        }

        String userIdStr = principal.getName();
        subject.setUserId(userIdStr);

        userRepository.findById(UserId.of(userIdStr)).ifPresent(user -> {
            List<String> roles = roleRepository.findAllByIds(user.getRoleIds()).stream()
                    .map(r -> r.getName())
                    .toList();
            subject.setRoles(roles);
        });

        if (subject.getRoles() == null) {
            subject.setRoles(List.of());
        }

        return subject;
    }
}
