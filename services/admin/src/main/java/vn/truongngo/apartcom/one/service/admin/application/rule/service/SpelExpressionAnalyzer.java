package vn.truongngo.apartcom.one.service.admin.application.rule.service;

import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.*;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Static SpEL AST walker shared by impact preview, reverse lookup, and UIElement coverage checks.
 *
 * SpEL AST structure key facts:
 *   - {@code a.b.method(arg)} → {@code CompoundExpression[ PFR("a"), PFR("b"), MR("method")[ StringLiteral ] ]}
 *   - {@code MethodReference} children are call ARGUMENTS (not receiver)
 *   - {@code PropertyOrFieldReference} has no children in compound chains
 */
public class SpelExpressionAnalyzer {

    public record AnalysisResult(
            List<String> requiredRoles,
            List<String> requiredAttributes,
            List<String> specificActions,
            boolean navigableWithoutData,
            boolean hasInstanceCondition,
            String parseWarning
    ) {
        public static AnalysisResult empty() {
            return new AnalysisResult(List.of(), List.of(), List.of(), false, false, null);
        }

        public static AnalysisResult parseError(String msg) {
            return new AnalysisResult(List.of(), List.of(), List.of(), false, false, msg);
        }
    }

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /**
     * Analyzes one or more SpEL expressions and returns the combined analysis result.
     */
    public static AnalysisResult analyze(String... expressions) {
        List<String> requiredRoles      = new ArrayList<>();
        List<String> requiredAttributes = new ArrayList<>();
        List<String> specificActions    = new ArrayList<>();
        boolean[] navigable             = {false};
        boolean[] hasInstance           = {false};

        for (String expression : expressions) {
            if (expression == null || expression.isBlank()) continue;
            try {
                SpelExpression parsed = (SpelExpression) PARSER.parseExpression(expression);
                walkNode(parsed.getAST(), requiredRoles, requiredAttributes,
                        specificActions, navigable, hasInstance);
            } catch (Exception e) {
                return AnalysisResult.parseError("Expression could not be parsed: " + e.getMessage());
            }
        }

        return new AnalysisResult(
                List.copyOf(requiredRoles),
                List.copyOf(requiredAttributes),
                List.copyOf(specificActions),
                navigable[0],
                hasInstance[0],
                null
        );
    }

    // -----------------------------------------------------------------------
    // Recursive AST walker
    // -----------------------------------------------------------------------

    private static void walkNode(SpelNode node,
                                 List<String> requiredRoles,
                                 List<String> requiredAttributes,
                                 List<String> specificActions,
                                 boolean[] navigable,
                                 boolean[] hasInstance) {
        if (node == null) return;

        if (node instanceof CompoundExpression ce) {
            analyzeCompound(ce, requiredRoles, requiredAttributes, specificActions, navigable, hasInstance);
        } else if (node instanceof OpEQ eq) {
            if (analyzeOpEQ(eq, specificActions, navigable)) return;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            walkNode(node.getChild(i), requiredRoles, requiredAttributes,
                    specificActions, navigable, hasInstance);
        }
    }

    // -----------------------------------------------------------------------
    // CompoundExpression pattern matching
    // -----------------------------------------------------------------------

    private static void analyzeCompound(CompoundExpression ce,
                                        List<String> requiredRoles,
                                        List<String> requiredAttributes,
                                        List<String> specificActions,
                                        boolean[] navigable,
                                        boolean[] hasInstance) {
        int count = ce.getChildCount();
        if (count < 2) return;

        SpelNode c0 = ce.getChild(0);
        SpelNode c1 = ce.getChild(1);

        // subject.roles.contains('X')
        if (count >= 3 && isPFR(c0, "subject") && isPFR(c1, "roles")) {
            SpelNode c2 = ce.getChild(2);
            if (isMR(c2, "contains")) {
                String role = stringArg(c2, 0);
                if (role != null && !requiredRoles.contains(role)) requiredRoles.add(role);
                return;
            }
        }

        // subject.getAttribute('X')
        if (isPFR(c0, "subject") && isMR(c1, "getAttribute")) {
            String attr = stringArg(c1, 0);
            if (attr != null && !requiredAttributes.contains(attr)) requiredAttributes.add(attr);
            return;
        }

        // object.data.someField → count >= 3
        if (isPFR(c0, "object") && isPFR(c1, "data") && count >= 3) {
            hasInstance[0] = true;
            return;
        }
    }

    // -----------------------------------------------------------------------
    // OpEQ pattern matching — returns true if recursion should be suppressed
    // -----------------------------------------------------------------------

    private static boolean analyzeOpEQ(OpEQ eq, List<String> specificActions, boolean[] navigable) {
        if (eq.getChildCount() < 2) return false;
        SpelNode left  = eq.getChild(0);
        SpelNode right = eq.getChild(1);

        // action.getAttribute('name') == 'X'
        String actionName = tryExtractActionName(left, right);
        if (actionName == null) actionName = tryExtractActionName(right, left);
        if (actionName != null) {
            if (!specificActions.contains(actionName)) specificActions.add(actionName);
            return true;
        }

        // object.data == null  →  navigable without data
        if (isObjectDataCompound(left) && right instanceof NullLiteral) {
            navigable[0] = true;
            return true;
        }
        if (isObjectDataCompound(right) && left instanceof NullLiteral) {
            navigable[0] = true;
            return true;
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String tryExtractActionName(SpelNode candidate, SpelNode valueSide) {
        if (!(candidate instanceof CompoundExpression ce)) return null;
        if (ce.getChildCount() < 2) return null;
        if (!isPFR(ce.getChild(0), "action")) return null;
        SpelNode mr = ce.getChild(1);
        if (!isMR(mr, "getAttribute")) return null;
        String argName = stringArg(mr, 0);
        if (!"name".equals(argName)) return null;
        return stringLiteralValue(valueSide);
    }

    private static boolean isObjectDataCompound(SpelNode node) {
        if (!(node instanceof CompoundExpression ce)) return false;
        if (ce.getChildCount() < 2) return false;
        return isPFR(ce.getChild(0), "object") && isPFR(ce.getChild(1), "data");
    }

    private static boolean isPFR(SpelNode node, String name) {
        return node instanceof PropertyOrFieldReference pfr && name.equals(pfr.getName());
    }

    private static boolean isMR(SpelNode node, String name) {
        return node instanceof MethodReference mr && name.equals(mr.getName());
    }

    private static String stringArg(SpelNode mr, int argIndex) {
        if (mr.getChildCount() <= argIndex) return null;
        return stringLiteralValue(mr.getChild(argIndex));
    }

    private static String stringLiteralValue(SpelNode node) {
        if (!(node instanceof StringLiteral sl)) return null;
        Object val = sl.getLiteralValue().getValue();
        return val instanceof String s ? s : null;
    }
}
