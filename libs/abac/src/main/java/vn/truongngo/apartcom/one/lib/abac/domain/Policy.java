package vn.truongngo.apartcom.one.lib.abac.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Represents an XACML Policy — contains a list of Rules.
 * @author Truong Ngo
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Policy extends AbstractPolicy {

    private List<Rule> rules;
}
