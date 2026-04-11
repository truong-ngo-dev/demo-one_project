package vn.truongngo.apartcom.one.lib.abac.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName;

/**
 * Abstract base class for policy elements (Policy and PolicySet).
 * @author Truong Ngo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.DEDUCTION,
        include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Policy.class, name = "policy"),
        @JsonSubTypes.Type(value = PolicySet.class, name = "policySet")
})
public abstract class AbstractPolicy implements Principle {

    private String id;
    private String description;
    private Expression target;
    private CombineAlgorithmName combineAlgorithmName;
    private Boolean isRoot;
}
