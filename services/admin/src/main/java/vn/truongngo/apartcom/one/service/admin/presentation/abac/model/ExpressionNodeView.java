package vn.truongngo.apartcom.one.service.admin.presentation.abac.model;

import vn.truongngo.apartcom.one.service.admin.application.expression.ExpressionTreeService;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionNode;

import java.util.List;

/**
 * Recursive response model for expression trees.
 * resolvedSpel is always present — FE uses it for preview and reverse-parse detection.
 */
public record ExpressionNodeView(
        String type,
        String name,
        String resolvedSpel,
        Long refId,
        String operator,
        List<ExpressionNodeView> children
) {
    public static ExpressionNodeView from(ExpressionNode node,
                                          ExpressionTreeService treeService,
                                          NamedExpressionRepository namedRepo) {
        if (node == null) return null;
        String resolvedSpel = treeService.resolveFromNode(node);
        return switch (node) {
            case ExpressionNode.Inline inline -> new ExpressionNodeView(
                    "INLINE", inline.name(), resolvedSpel, null, null, null);
            case ExpressionNode.LibraryRef ref -> {
                String libName = namedRepo.findById(ref.refId())
                        .map(ne -> ne.getName()).orElse(null);
                yield new ExpressionNodeView(
                        "LIBRARY_REF", libName, resolvedSpel, ref.refId().value(), null, null);
            }
            case ExpressionNode.Composition comp -> {
                List<ExpressionNodeView> childViews = comp.children().stream()
                        .map(c -> ExpressionNodeView.from(c, treeService, namedRepo))
                        .toList();
                yield new ExpressionNodeView(
                        "COMPOSITION", null, resolvedSpel, null,
                        comp.operator().name(), childViews);
            }
        };
    }
}
