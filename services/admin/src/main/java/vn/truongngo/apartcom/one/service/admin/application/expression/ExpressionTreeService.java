package vn.truongngo.apartcom.one.service.admin.application.expression;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpression;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionNode;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.AbacExpressionJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.AbacExpressionJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application-layer service responsible for persisting, resolving, and deleting
 * ExpressionNode trees in the abac_expression table.
 *
 * The domain layer has no DB ids; this service bridges domain ↔ infrastructure.
 */
@Service
@RequiredArgsConstructor
public class ExpressionTreeService {

    private final AbacExpressionJpaRepository expressionRepo;
    private final NamedExpressionRepository namedExpressionRepository;

    /**
     * Persist an ExpressionNode tree.
     * Returns the DB row id of the root node (to be stored in rule/policy FK columns).
     */
    public Long persist(ExpressionNode node, Long parentId) {
        if (node == null) return null;

        return switch (node) {
            case ExpressionNode.Inline inline -> promoteInline(inline, parentId);
            case ExpressionNode.LibraryRef ref -> persistLibraryRef(ref, parentId);
            case ExpressionNode.Composition comp -> persistComposition(comp, parentId);
        };
    }

    /**
     * Resolve an ExpressionNode tree to a flat SpEL string.
     * LibraryRef nodes are expanded via NamedExpressionRepository.
     * Used by AdminPolicyProvider at evaluation time.
     */
    public String resolveFromNode(ExpressionNode node) {
        if (node == null) return null;
        return switch (node) {
            case ExpressionNode.Inline inline -> inline.spel();
            case ExpressionNode.LibraryRef ref -> {
                NamedExpression named = namedExpressionRepository.findById(ref.refId())
                        .orElseThrow(() -> new IllegalStateException(
                                "NamedExpression not found: " + ref.refId().value()));
                yield named.getSpel();
            }
            case ExpressionNode.Composition comp -> {
                String sep = comp.operator() == ExpressionNode.Operator.AND ? " && " : " || ";
                String joined = comp.children().stream()
                        .map(this::resolveFromNode)
                        .collect(Collectors.joining(sep));
                yield "(" + joined + ")";
            }
        };
    }

    /**
     * Load an ExpressionNode tree from DB root id.
     * Used by PolicyMapper when reconstructing domain objects from persistence.
     */
    public ExpressionNode loadTree(Long rootId) {
        if (rootId == null) return null;
        AbacExpressionJpaEntity row = expressionRepo.findById(rootId)
                .orElseThrow(() -> new IllegalStateException("abac_expression not found: " + rootId));
        return toNode(row);
    }

    /**
     * Resolve a DB root id to a flat SpEL string.
     * Convenience method used where only the DB id is available.
     */
    public String resolveSpel(Long rootId) {
        if (rootId == null) return null;
        return resolveFromNode(loadTree(rootId));
    }

    /**
     * Delete an expression tree starting from rootId.
     * Behaviour per node type:
     *   - LITERAL → always a LibraryRef row. DELETE this row, never touch the NamedExpression AR.
     *   - COMPOSITION → recurse children, then DELETE this row.
     * Inline rows do not exist in DB, so no Inline case is handled here.
     */
    public void deleteTree(Long rootId) {
        if (rootId == null) return;
        AbacExpressionJpaEntity row = expressionRepo.findById(rootId).orElse(null);
        if (row == null) return;

        if (row.getType() == AbacExpressionJpaEntity.ExpressionType.COMPOSITION) {
            List<AbacExpressionJpaEntity> children = expressionRepo.findAllByParentId(rootId);
            for (AbacExpressionJpaEntity child : children) {
                deleteTree(child.getId());
            }
            expressionRepo.delete(row);
        } else {
            // LITERAL — always a LibraryRef row. Delete the row, never the NamedExpression AR.
            expressionRepo.delete(row);
        }
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    private Long promoteInline(ExpressionNode.Inline inline, Long parentId) {
        // Find existing NamedExpression by SpEL to avoid duplicates
        NamedExpression named = namedExpressionRepository.findBySpel(inline.spel())
                .orElseGet(() -> namedExpressionRepository.save(
                        NamedExpression.create(inline.name(), inline.spel())));
        // Persist as LibraryRef
        return persistLibraryRef(new ExpressionNode.LibraryRef(named.getId()), parentId);
    }

    private Long persistLibraryRef(ExpressionNode.LibraryRef ref, Long parentId) {
        AbacExpressionJpaEntity entity = new AbacExpressionJpaEntity();
        entity.setType(AbacExpressionJpaEntity.ExpressionType.LITERAL);
        entity.setNamedExpressionId(ref.refId().value());
        entity.setParentId(parentId);
        return expressionRepo.save(entity).getId();
    }

    private Long persistComposition(ExpressionNode.Composition comp, Long parentId) {
        AbacExpressionJpaEntity entity = new AbacExpressionJpaEntity();
        entity.setType(AbacExpressionJpaEntity.ExpressionType.COMPOSITION);
        entity.setCombinationType(comp.operator() == ExpressionNode.Operator.AND
                ? AbacExpressionJpaEntity.CombinationType.AND
                : AbacExpressionJpaEntity.CombinationType.OR);
        entity.setParentId(parentId);
        AbacExpressionJpaEntity saved = expressionRepo.save(entity);
        Long compositionId = saved.getId();

        for (ExpressionNode child : comp.children()) {
            persist(child, compositionId);
        }
        return compositionId;
    }

    private ExpressionNode toNode(AbacExpressionJpaEntity row) {
        if (row.getType() == AbacExpressionJpaEntity.ExpressionType.COMPOSITION) {
            List<AbacExpressionJpaEntity> childRows = expressionRepo.findAllByParentId(row.getId());
            List<ExpressionNode> children = new ArrayList<>();
            for (AbacExpressionJpaEntity child : childRows) {
                children.add(toNode(child));
            }
            ExpressionNode.Operator op = row.getCombinationType() == AbacExpressionJpaEntity.CombinationType.AND
                    ? ExpressionNode.Operator.AND : ExpressionNode.Operator.OR;
            return new ExpressionNode.Composition(op, children);
        }
        // LITERAL — always a LibraryRef (Inline is never persisted)
        if (row.getNamedExpressionId() == null) {
            throw new IllegalStateException(
                    "abac_expression LITERAL row id=" + row.getId() + " has no named_expression_id. " +
                            "Inline rows must not exist in DB.");
        }
        return new ExpressionNode.LibraryRef(NamedExpressionId.of(row.getNamedExpressionId()));
    }
}
