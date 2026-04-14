package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class PolicyException extends DomainException {

    public PolicyException(PolicyErrorCode errorCode) {
        super(errorCode);
    }

    public static PolicyException policyNotFound() {
        return new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND);
    }

    public static PolicyException ruleNotFound() {
        return new PolicyException(PolicyErrorCode.RULE_NOT_FOUND);
    }

    public static PolicyException invalidSpElExpression() {

        return new PolicyException(PolicyErrorCode.INVALID_SPEL_EXPRESSION);
    }

    public static Exception policySetNameDuplicate() {
        return new PolicyException(PolicyErrorCode.DUPLICATE_POLICY_NAME);
    }
}
