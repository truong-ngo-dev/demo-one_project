package vn.truongngo.apartcom.one.service.admin.domain.abac.policy;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacErrorCode;

public class AbacPolicyException extends DomainException {

    public AbacPolicyException(AbacErrorCode errorCode) {
        super(errorCode);
    }

    public static AbacPolicyException policySetNotFound() {
        return new AbacPolicyException(AbacErrorCode.POLICY_SET_NOT_FOUND);
    }

    public static AbacPolicyException policySetNameDuplicate() {
        return new AbacPolicyException(AbacErrorCode.POLICY_SET_NAME_DUPLICATE);
    }

    public static AbacPolicyException policyNotFound() {
        return new AbacPolicyException(AbacErrorCode.POLICY_NOT_FOUND);
    }

    public static AbacPolicyException ruleNotFound() {
        return new AbacPolicyException(AbacErrorCode.RULE_NOT_FOUND);
    }

    public static AbacPolicyException invalidSpelExpression() {
        return new AbacPolicyException(AbacErrorCode.INVALID_SPEL_EXPRESSION);
    }
}
