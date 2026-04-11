package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy_set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy_set")
@Getter
@Setter
@NoArgsConstructor
public class PolicySetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "scope", nullable = false)
    private String scope;

    @Column(name = "combine_algorithm", nullable = false, length = 50)
    private String combineAlgorithm;

    @Column(name = "is_root", nullable = false)
    private boolean isRoot;

    @Column(name = "tenant_id", length = 100)
    private String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;
}
