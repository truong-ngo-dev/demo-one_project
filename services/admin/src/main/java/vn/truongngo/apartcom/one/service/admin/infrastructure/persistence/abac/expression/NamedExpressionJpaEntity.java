package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "named_expression")
@Getter
@Setter
@NoArgsConstructor
public class NamedExpressionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "spel", nullable = false, columnDefinition = "TEXT")
    private String spel;
}
