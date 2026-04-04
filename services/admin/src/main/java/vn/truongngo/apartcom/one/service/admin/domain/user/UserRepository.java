package vn.truongngo.apartcom.one.service.admin.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.truongngo.apartcom.one.lib.common.domain.service.Repository;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;

import java.util.Optional;

public interface UserRepository extends Repository<User, UserId> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findBySocialConnection(String provider, String socialId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByRoleId(RoleId roleId);

    Page<User> findAll(String keyword, UserStatus status, RoleId roleId, Pageable pageable);

}
