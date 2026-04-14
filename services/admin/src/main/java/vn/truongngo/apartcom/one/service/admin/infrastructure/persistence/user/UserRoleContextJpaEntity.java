package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Scope;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role.RoleJpaEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "user_role_context",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_role_context",
        columnNames = {"user_id", "scope", "org_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class UserRoleContextJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private Scope scope;

    /**
     * Stored as empty string '' for ADMIN scope (orgId = null in domain).
     * MySQL UNIQUE constraint does not consider NULL == NULL, so we use '' as sentinel.
     */
    @Column(name = "org_id", nullable = false, length = 100)
    private String orgId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role_context_roles",
        joinColumns = @JoinColumn(name = "context_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleJpaEntity> roles = new HashSet<>();
}
