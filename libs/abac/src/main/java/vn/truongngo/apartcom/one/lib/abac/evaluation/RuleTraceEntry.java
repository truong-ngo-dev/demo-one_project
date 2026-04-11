package vn.truongngo.apartcom.one.lib.abac.evaluation;

import vn.truongngo.apartcom.one.lib.abac.domain.Rule;

/**
 * A single trace entry recording how one Rule was evaluated.
 * Collected during {@code authorizeWithTrace()} in PdpEngine.
 */
public record RuleTraceEntry(
        String ruleId,
        String ruleDescription,
        Rule.Effect effect,
        boolean targetMatched,
        Boolean conditionMatched,   // null when target did not match (condition not evaluated)
        boolean wasDeciding         // true when this rule produced a PERMIT or DENY result
) {}
