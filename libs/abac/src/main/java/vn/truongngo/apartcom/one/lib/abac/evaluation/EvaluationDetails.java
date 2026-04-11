package vn.truongngo.apartcom.one.lib.abac.evaluation;

import java.util.List;

/**
 * Carries evaluation details when tracing is enabled.
 * Returned in {@link vn.truongngo.apartcom.one.lib.abac.pdp.AuthzDecision#getDetails()}
 * when using {@code PdpEngine.authorizeWithTrace()}.
 */
public record EvaluationDetails(Object cause, List<RuleTraceEntry> trace) {}
