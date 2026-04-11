package vn.truongngo.apartcom.one.lib.abac.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.truongngo.apartcom.one.lib.abac.domain.Expression;

import java.util.List;

/**
 * Represents the cause of an indeterminate result in evaluation.
 * @author Truong Ngo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndeterminateCause {

    public static final String DEFAULT_DESC_FORMAT = "%s with id %s has %s";

    public enum IndeterminateCauseType {
        SYNTAX_ERROR,
        PROCESSING_ERROR
    }

    private IndeterminateCauseType code;
    private String description;
    private String content;
    private List<IndeterminateCause> subIndeterminateCauses;

    public IndeterminateCause(IndeterminateCauseType code) {
        this.code = code;
    }

    public IndeterminateCause(IndeterminateCauseType code, List<IndeterminateCause> subIndeterminateCauses) {
        this.code = code;
        this.subIndeterminateCauses = subIndeterminateCauses;
    }

    public void buildDefaultDescription(String element, String id) {
        description = String.format(DEFAULT_DESC_FORMAT, element, id, code.name().toLowerCase());
    }

    public void buildDefaultContent(Expression expression) {
        content = expression.getExpression();
    }
}
