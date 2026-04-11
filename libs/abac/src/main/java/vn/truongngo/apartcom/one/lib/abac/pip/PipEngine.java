package vn.truongngo.apartcom.one.lib.abac.pip;

import vn.truongngo.apartcom.one.lib.abac.context.Environment;
import vn.truongngo.apartcom.one.lib.abac.context.Subject;
import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;
import vn.truongngo.apartcom.one.lib.abac.rap.ResourceAccessConfig;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Policy Information Point (PIP) engine — central point for fetching policy-related information.
 * @author Truong Ngo
 */
public record PipEngine(PolicyProvider policyProvider, EnvironmentProvider environmentProvider,
                        SubjectProvider subjectProvider, ResourceAccessConfig resourceAccessConfig) {

    public AbstractPolicy getPolicy(String serviceName) {
        return policyProvider.getPolicy(serviceName);
    }

    public Environment getEnvironment(String serviceName) {
        return environmentProvider.getEnvironment(serviceName);
    }

    public Subject getSubject(Principal principal) {
        return subjectProvider.getSubject(principal);
    }

    public String getResourceName(String path) {
        Pattern pattern = Pattern.compile(resourceAccessConfig.getResourceNameExtractor());
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? matcher.group(1) : "";
    }
}
