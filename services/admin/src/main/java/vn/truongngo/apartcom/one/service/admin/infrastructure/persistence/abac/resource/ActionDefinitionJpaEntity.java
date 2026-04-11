package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.resource;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "action_definition")
@Getter
@Setter
@NoArgsConstructor
public class ActionDefinitionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private ResourceDefinitionJpaEntity resource;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_standard", nullable = false)
    private boolean isStandard;
}
