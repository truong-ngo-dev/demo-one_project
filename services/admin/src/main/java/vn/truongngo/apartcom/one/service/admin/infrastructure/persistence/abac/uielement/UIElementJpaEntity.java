package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.uielement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementType;

@Entity
@Table(name = "ui_element")
@Getter
@Setter
@NoArgsConstructor
public class UIElementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "element_id", nullable = false, unique = true, length = 200)
    private String elementId;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private UIElementType type;

    @Column(name = "element_group", length = 100)
    private String elementGroup;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "action_id", nullable = false)
    private Long actionId;
}
