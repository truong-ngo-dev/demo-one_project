package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy")
@Getter
@Setter
@NoArgsConstructor
public class PolicyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "policy_set_id", nullable = false)
    private Long policySetId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "target_expression_id")
    private Long targetExpressionId;

    @Column(name = "combine_algorithm", nullable = false, length = 50)
    private String combineAlgorithm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;
}
