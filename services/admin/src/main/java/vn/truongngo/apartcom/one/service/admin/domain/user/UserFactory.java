package vn.truongngo.apartcom.one.service.admin.domain.user;

import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;

import java.time.Instant;
import java.util.Set;

public class UserFactory {

    public static User register(String username,
                                String email,
                                String phoneNumber,
                                String fullName,
                                UserPassword password,
                                Set<RoleId> roleIds) {
        Assert.isTrue(
                username != null || email != null || phoneNumber != null,
                "at least one identifier is required"
        );

        User user = new User(UserId.generate(), username, email, phoneNumber, fullName, password);
        if (!roleIds.isEmpty()) {
            user.addRoleContext(Scope.ADMIN, null, roleIds);
        }
        user.registerEvent(new UserCreatedEvent(
                user.getId().getValue(),
                username,
                email,
                phoneNumber,
                UserCreatedEvent.RegistrationMethod.DEFAULT
        ));
        return user;
    }

    public static User adminCreate(String username,
                                   String email,
                                   String fullName,
                                   UserPassword password,
                                   Set<RoleId> roleIds) {
        Assert.hasText(username, "username is required");
        Assert.hasText(email, "email is required");

        User user = new User(UserId.generate(), username, email, null, fullName, password);
        if (!roleIds.isEmpty()) {
            user.addRoleContext(Scope.ADMIN, null, roleIds);
        }
        user.registerEvent(new UserCreatedEvent(
                user.getId().getValue(),
                username,
                email,
                null,
                UserCreatedEvent.RegistrationMethod.ADMIN
        ));
        return user;
    }

    public static User socialRegister(String username, String email,
                                      String provider, String socialId,
                                      String providerEmail, Instant connectedAt) {
        Assert.hasText(username, "username is required");
        Assert.hasText(email, "email is required");

        User user = new User(UserId.generate(), username, email, null, null, null);
        user.connectSocial(provider, socialId, providerEmail, connectedAt);
        user.registerEvent(new UserCreatedEvent(
                user.getId().getValue(),
                username,
                email,
                null,
                UserCreatedEvent.RegistrationMethod.SOCIAL
        ));
        return user;
    }
}
