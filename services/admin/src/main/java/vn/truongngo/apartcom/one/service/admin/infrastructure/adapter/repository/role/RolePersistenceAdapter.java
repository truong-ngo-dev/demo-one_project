package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.role;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.role.Role;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role.RoleJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role.RoleMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RoleRepository {

    private final RoleJpaRepository jpaRepository;

    @Override
    public void save(Role role) {
        jpaRepository.save(RoleMapper.toEntity(role));
    }

    @Override
    public Optional<Role> findById(RoleId id) {
        return jpaRepository.findById(id.getValue()).map(RoleMapper::toDomain);
    }

    @Override
    public void delete(RoleId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public Set<Role> findAllByIds(Set<RoleId> ids) {
        Set<String> idValues = ids.stream()
                .map(RoleId::getValue)
                .collect(Collectors.toSet());
        return jpaRepository.findAllById(idValues).stream()
                .map(RoleMapper::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream()
                .map(RoleMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Role> findAll(String keyword, Pageable pageable) {
        return jpaRepository.search(keyword, pageable).map(RoleMapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}
