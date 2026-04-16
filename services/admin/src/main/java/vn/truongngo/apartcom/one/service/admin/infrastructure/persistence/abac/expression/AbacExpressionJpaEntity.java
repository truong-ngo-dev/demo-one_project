package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "abac_expression")
@Getter
@Setter
@NoArgsConstructor
public class AbacExpressionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpressionType type;

    @Column(name = "spel_expression", columnDefinition = "TEXT")
    private String spelExpression;

    @Column(name = "combination_type")
    @Enumerated(EnumType.STRING)
    private CombinationType combinationType;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "named_expression_id")
    private Long namedExpressionId;

    public enum ExpressionType {
        LITERAL, COMPOSITION
    }

    public enum CombinationType {
        AND, OR
    }
}
