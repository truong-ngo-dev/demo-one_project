package vn.truongngo.apartcom.one.service.admin.domain.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.truongngo.apartcom.one.lib.common.domain.service.Repository;

import java.util.List;
import java.util.Set;

public interface RoleRepository extends Repository<Role, RoleId> {

    Set<Role> findAllByIds(Set<RoleId> ids);

    List<Role> findAll();

    Page<Role> findAll(String keyword, Pageable pageable);

    boolean existsByName(String name);
}
