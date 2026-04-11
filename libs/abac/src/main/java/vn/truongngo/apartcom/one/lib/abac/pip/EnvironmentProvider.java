package vn.truongngo.apartcom.one.lib.abac.pip;

import vn.truongngo.apartcom.one.lib.abac.context.Environment;

/**
 * Provides access to the environment context for a given service in the ABAC system.
 * @author Truong Ngo
 */
public interface EnvironmentProvider {

    Environment getEnvironment(String serviceName);
}
