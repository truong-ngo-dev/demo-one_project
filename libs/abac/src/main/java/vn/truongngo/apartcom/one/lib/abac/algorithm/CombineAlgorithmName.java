package vn.truongngo.apartcom.one.lib.abac.algorithm;

/**
 * Enum representing the names of different combination algorithms.
 * @author Truong Ngo
 */
public enum CombineAlgorithmName {
    DENY_OVERRIDES,
    PERMIT_OVERRIDES,
    DENY_UNLESS_PERMIT,
    PERMIT_UNLESS_DENY,
    FIRST_APPLICABLE,
    ONLY_ONE_APPLICABLE
}
