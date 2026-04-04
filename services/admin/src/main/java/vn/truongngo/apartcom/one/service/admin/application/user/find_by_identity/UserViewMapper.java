package vn.truongngo.apartcom.one.service.admin.application.user.find_by_identity;

import vn.truongngo.apartcom.one.service.admin.domain.role.Role;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;

import java.util.Set;
import java.util.stream.Collectors;

public class UserViewMapper {

    public static UserView toView(User user, Set<Role> roles) {

        Set<UserView.RoleView> roleViews = roles.stream()
                .map(r -> new UserView.RoleView(r.getId().getValue(), r.getName()))
                .collect(Collectors.toSet());

        Set<UserView.SocialConnectionView> socialViews = user.getSocialConnections().stream()
                .map(s -> new UserView.SocialConnectionView(s.getProvider(), s.getSocialId()))
                .collect(Collectors.toSet());

        return new UserView(
                user.getId().getValue(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getPassword() != null ? user.getPassword().getHashedValue() : null,
                user.getStatus(),
                roleViews,
                socialViews
        );
    }
}
