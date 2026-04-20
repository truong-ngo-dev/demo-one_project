package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContextStatus;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByUsername(String username);
    Optional<UserJpaEntity> findByEmail(String email);
    Optional<UserJpaEntity> findByPhoneNumber(String phoneNumber);

    @Query("""
        SELECT u FROM UserJpaEntity u
        JOIN u.socialConnections s
        WHERE s.provider = :provider AND s.socialId = :socialId
    """)
    Optional<UserJpaEntity> findBySocialConnection(
            @Param("provider") String provider,
            @Param("socialId") String socialId
    );

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    @Query("""
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
        FROM UserJpaEntity u
        JOIN u.roleContexts ctx
        JOIN ctx.roles r
        WHERE r.id = :roleId
    """)
    boolean existsUserWithRole(@Param("roleId") String roleId);

    @Query("""
        SELECT COUNT(u)
        FROM UserJpaEntity u
        JOIN u.roleContexts ctx
        JOIN ctx.roles r
        WHERE r.name = :roleName
    """)
    long countByRoleName(@Param("roleName") String roleName);

    @Query(
        value = """
            SELECT u FROM UserJpaEntity u
            WHERE (:keyword IS NULL OR
                LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:status IS NULL OR u.status = :status)
            AND (:roleId IS NULL OR EXISTS (
                SELECT 1 FROM UserJpaEntity u2
                JOIN u2.roleContexts ctx
                JOIN ctx.roles r
                WHERE u2.id = u.id AND r.id = :roleId
            ))
            """,
        countQuery = """
            SELECT COUNT(u) FROM UserJpaEntity u
            WHERE (:keyword IS NULL OR
                LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:status IS NULL OR u.status = :status)
            AND (:roleId IS NULL OR EXISTS (
                SELECT COUNT(r2) FROM UserJpaEntity u2
                JOIN u2.roleContexts ctx2
                JOIN ctx2.roles r2
                WHERE u2.id = u.id AND r2.id = :roleId
            ))
            """
    )
    Page<UserJpaEntity> searchUsers(
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            @Param("roleId") String roleId,
            Pageable pageable
    );

    Optional<UserJpaEntity> findByPartyId(String partyId);

    @Query("""
        SELECT DISTINCT u FROM UserJpaEntity u
        JOIN u.roleContexts rc
        WHERE rc.scope = :scope AND rc.orgId = :orgId AND rc.status = :status
    """)
    List<UserJpaEntity> findAllByActiveRoleContext(
            @Param("scope") Scope scope,
            @Param("orgId") String orgId,
            @Param("status") RoleContextStatus status
    );
}
