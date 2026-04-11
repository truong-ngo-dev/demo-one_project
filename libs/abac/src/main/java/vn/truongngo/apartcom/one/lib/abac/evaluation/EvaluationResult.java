package vn.truongngo.apartcom.one.lib.abac.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of an evaluation in a decision-making system.
 * @author Truong Ngo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class EvaluationResult {

    private EvaluationResultType result;
    private IndeterminateCause indeterminateCause;

    public EvaluationResult(EvaluationResultType result) {
        this.result = result;
    }

    public boolean isIndeterminate() {
        return result.isIndeterminate();
    }

    public boolean isPermit() {
        return result.isPermit();
    }

    public boolean isDeny() {
        return result.isDeny();
    }

    public boolean isNotApplicable() {
        return result.isNotApplicable();
    }

    public enum EvaluationResultType {
        PERMIT, DENY, NOT_APPLICABLE, INDETERMINATE,
        INDETERMINATE_D, INDETERMINATE_P, INDETERMINATE_DP;

        public boolean isIndeterminate() {
            return this.equals(INDETERMINATE) || this.equals(INDETERMINATE_D) ||
                   this.equals(INDETERMINATE_P) || this.equals(INDETERMINATE_DP);
        }

        public boolean isPermit() {
            return this.equals(PERMIT);
        }

        public boolean isDeny() {
            return this.equals(DENY);
        }

        public boolean isNotApplicable() {
            return this.equals(NOT_APPLICABLE);
        }
    }
}
