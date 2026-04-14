package vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.truongngo.apartcom.one.lib.abac.pdp.DecisionStrategy;
import vn.truongngo.apartcom.one.lib.abac.pdp.PdpConfiguration;
import vn.truongngo.apartcom.one.lib.abac.pdp.PdpEngine;
import vn.truongngo.apartcom.one.lib.abac.pep.PepEngine;
import vn.truongngo.apartcom.one.lib.abac.pip.EnvironmentProvider;
import vn.truongngo.apartcom.one.lib.abac.pip.PipEngine;
import vn.truongngo.apartcom.one.lib.abac.pip.PolicyProvider;
import vn.truongngo.apartcom.one.lib.abac.pip.SubjectProvider;

@Configuration
public class AbacConfig {

    /** Used by AdminPolicyProvider (simulation) and PipEngine. */
    @Bean
    public PdpEngine pdpEngine() {
        PdpConfiguration configuration = new PdpConfiguration(DecisionStrategy.DEFAULT_DENY);
        return new PdpEngine(configuration);
    }

    @Bean
    public PipEngine pipEngine(PolicyProvider policyProvider,
                                EnvironmentProvider environmentProvider,
                                SubjectProvider subjectProvider) {
        return new PipEngine(policyProvider, environmentProvider, subjectProvider);
    }

    @Bean
    public PepEngine pepEngine() {
        return new PepEngine(DecisionStrategy.DEFAULT_DENY);
    }
}
