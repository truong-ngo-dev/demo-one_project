package vn.truongngo.apartcom.one.lib.abac.pip;

import vn.truongngo.apartcom.one.lib.abac.context.Environment;
import vn.truongngo.apartcom.one.lib.abac.context.Subject;
import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;

import java.security.Principal;

/**
 * Policy Information Point (PIP) engine — central point for fetching policy-related information.
 * Resource/action resolution is handled entirely via @ResourceMapping on controller methods.
 * @author Truong Ngo
 */
public record PipEngine(PolicyProvider policyProvider, EnvironmentProvider environmentProvider,
                        SubjectProvider subjectProvider) {

    public AbstractPolicy getPolicy(String serviceName) {
        return policyProvider.getPolicy(serviceName);
    }

    public Environment getEnvironment(String serviceName) {
        return environmentProvider.getEnvironment(serviceName);
    }

    public Subject getSubject(Principal principal) {
        return subjectProvider.getSubject(principal);
    }
}
