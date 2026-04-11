package vn.truongngo.apartcom.one.lib.abac.pep;

import lombok.extern.slf4j.Slf4j;
import vn.truongngo.apartcom.one.lib.abac.pdp.AuthzDecision;
import vn.truongngo.apartcom.one.lib.abac.pdp.AuthzRequest;
import vn.truongngo.apartcom.one.lib.abac.pdp.DecisionStrategy;
import vn.truongngo.apartcom.one.lib.abac.pdp.PdpConfiguration;
import vn.truongngo.apartcom.one.lib.abac.pdp.PdpEngine;
import vn.truongngo.apartcom.one.lib.abac.utils.StringUtils;

import java.util.stream.Stream;

/**
 * Policy Enforcement Point (PEP) engine — enforces authorization decisions.
 * @author Truong Ngo
 */
@Slf4j
public class PepEngine {

    private final PdpEngine pdpEngine;

    public PepEngine(DecisionStrategy decisionStrategy) {
        this.pdpEngine = new PdpEngine(new PdpConfiguration(decisionStrategy));
    }

    public AuthzDecision enforce(AuthzRequest authzRequest, String[] ignoredPath) {
        log.info("enforce authz request: {}", authzRequest);
        String path = authzRequest.getAction().getRequest().getRequestedURI();
        boolean ignore = Stream.of(ignoredPath).anyMatch(i -> StringUtils.matchUrlPath(i, path));
        if (ignore) {
            log.info("Ignore enforce for path: {}", path);
            return new AuthzDecision(AuthzDecision.Decision.PERMIT, "Ignore path: " + path);
        }
        AuthzDecision decision = pdpEngine.authorize(authzRequest);
        log.info("enforce decision: {}", decision.getDecision());
        return decision;
    }
}
