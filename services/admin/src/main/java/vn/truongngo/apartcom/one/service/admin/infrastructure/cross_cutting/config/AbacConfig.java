package vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.truongngo.apartcom.one.lib.abac.pdp.DecisionStrategy;
import vn.truongngo.apartcom.one.lib.abac.pdp.PdpConfiguration;
import vn.truongngo.apartcom.one.lib.abac.pdp.PdpEngine;

@Configuration
public class AbacConfig {

    @Bean
    public PdpEngine pdpEngine() {
        PdpConfiguration configuration = new PdpConfiguration(DecisionStrategy.DEFAULT_DENY);
        return new PdpEngine(configuration);
    }
}
