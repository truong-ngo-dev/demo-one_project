package vn.truongngo.apartcom.one.service.admin.domain.user;

import lombok.Getter;
//import org.springframework.security.crypto.password.PasswordEncoder;
import vn.truongngo.apartcom.one.lib.common.domain.model.ValueObject;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;

@Getter
public class UserPassword implements ValueObject {

    private final String hashedValue;

    private UserPassword(String hashedValue) {
        this.hashedValue = hashedValue;
    }

    public static UserPassword ofHashed(String hashedValue) {
        Assert.hasText(hashedValue, "hashedValue is required");
        return new UserPassword(hashedValue);
    }

//    public boolean matches(String rawPassword, PasswordEncoder encoder) {
//        return encoder.matches(rawPassword, this.hashedValue);
//    }

}
