package vn.truongngo.apartcom.one.lib.abac.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Represents an XACML PolicySet — container for multiple policies or policy sets.
 * @author Truong Ngo
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PolicySet extends AbstractPolicy {

    private List<AbstractPolicy> policies;
}
