package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, String> {

    boolean existsByName(String name);

    Optional<RoleJpaEntity> findByName(String name);

    @Query("SELECT r FROM RoleJpaEntity r WHERE " +
           ":keyword IS NULL OR " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<RoleJpaEntity> search(@Param("keyword") String keyword, Pageable pageable);
}
