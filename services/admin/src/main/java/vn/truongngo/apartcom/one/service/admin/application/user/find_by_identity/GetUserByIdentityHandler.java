package vn.truongngo.apartcom.one.service.admin.application.user.find_by_identity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;
import vn.truongngo.apartcom.one.service.admin.domain.role.Role;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserErrorCode;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetUserByIdentityHandler implements CommandHandler<GetUserByIdentityQuery, UserView> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserView handle(GetUserByIdentityQuery query) {
        User user = resolve(query.identity());
        if (user.isLocked()) {
            throw UserException.locked();
        }
        Set<Role> roles = roleRepository.findAllByIds(user.getRoleIds());
        return UserViewMapper.toView(user, roles);
    }

    private User resolve(String identity) {
        if (identity.contains("@")) {
            return userRepository.findByEmail(identity).orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));
        }
        return userRepository.findByUsername(identity).orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));
    }
}
