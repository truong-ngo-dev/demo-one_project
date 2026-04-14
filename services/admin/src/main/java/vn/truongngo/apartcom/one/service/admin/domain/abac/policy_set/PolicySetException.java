package vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class PolicySetException extends DomainException {

    public PolicySetException(PolicySetErrorCode errorCode) {
        super(errorCode);
    }

    public static PolicySetException policySetNotFound() {
        return new PolicySetException(PolicySetErrorCode.POLICY_SET_NOT_FOUND);
    }

    public static PolicySetException policySetNameDuplicate() {
        return new PolicySetException(PolicySetErrorCode.POLICY_SET_NAME_DUPLICATE);
    }
}
