package vn.truongngo.apartcom.one.lib.abac.context;

import lombok.Data;
import vn.truongngo.apartcom.one.lib.abac.evaluation.RuleTraceEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the context for evaluating access control decisions (subject, resource, action, environment).
 * Also serves as the trace collector when tracing is enabled via {@link #enableTracing()}.
 *
 * @author Truong Ngo
 */
@Data
public class EvaluationContext {

    public Subject subject;
    public Resource object;
    public Action action;
    public Environment environment;

    private List<RuleTraceEntry> traceEntries = new ArrayList<>();
    private boolean tracingEnabled = false;

    public EvaluationContext(Subject subject, Resource object, Action action, Environment environment) {
        this.subject = subject;
        this.object = object;
        this.action = action;
        this.environment = environment;
    }

    public void enableTracing() {
        this.tracingEnabled = true;
    }

    public void addTraceEntry(RuleTraceEntry entry) {
        if (tracingEnabled) {
            traceEntries.add(entry);
        }
    }

    public List<RuleTraceEntry> getTraceEntries() {
        return Collections.unmodifiableList(traceEntries);
    }
}
