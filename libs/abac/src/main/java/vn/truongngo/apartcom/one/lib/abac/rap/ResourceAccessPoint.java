package vn.truongngo.apartcom.one.lib.abac.rap;

/**
 * Registry for managing ResourceAccessMetadata objects.
 * @author Truong Ngo
 */
public interface ResourceAccessPoint {

    ResourceAccessMetadata getResourceAccessMetadata(String resourceKey);
}
