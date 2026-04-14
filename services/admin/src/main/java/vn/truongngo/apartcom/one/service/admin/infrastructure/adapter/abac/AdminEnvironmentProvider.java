package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.abac;

import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.abac.context.Environment;
import vn.truongngo.apartcom.one.lib.abac.pip.EnvironmentProvider;

import java.util.HashMap;

/**
 * Returns an empty Environment for admin-service.
 * Phase 2: add service metadata if needed.
 */
@Component
public class AdminEnvironmentProvider implements EnvironmentProvider {

    @Override
    public Environment getEnvironment(String serviceName) {
        Environment env = new Environment();
        env.setGlobal(new HashMap<>());
        env.setService(new HashMap<>());
        return env;
    }
}
