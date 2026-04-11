package vn.truongngo.apartcom.one.lib.abac.domain;

import lombok.Data;

/**
 * Represents an XACML Rule element.
 * @author Truong Ngo
 */
@Data
public class Rule implements Principle {

    private String id;
    private String description;
    private Expression target;
    private Expression condition;
    private Effect effect;

    public enum Effect {
        PERMIT,
        DENY
    }
}
