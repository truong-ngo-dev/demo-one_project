package vn.truongngo.apartcom.one.lib.abac.pip;

import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;

/**
 * Provides access to the policy associated with a given service in the ABAC system.
 * @author Truong Ngo
 */
public interface PolicyProvider {

    AbstractPolicy getPolicy(String serviceName);
}
