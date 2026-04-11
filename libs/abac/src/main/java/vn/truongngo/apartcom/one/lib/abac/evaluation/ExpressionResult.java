package vn.truongngo.apartcom.one.lib.abac.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of evaluating an Expression.
 * @author Truong Ngo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionResult {

    private ResultType resultType;
    private IndeterminateCause indeterminateCause;

    public ExpressionResult(ResultType resultType) {
        this.resultType = resultType;
    }

    public boolean isIndeterminate() {
        return resultType == ResultType.INDETERMINATE;
    }

    public boolean isNotMatch() {
        return resultType == ResultType.NO_MATCH;
    }

    public boolean isMatch() {
        return resultType == ResultType.MATCH;
    }

    public enum ResultType {
        MATCH,
        NO_MATCH,
        INDETERMINATE
    }
}
