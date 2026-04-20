package vn.truongngo.apartcom.one.service.party.domain.employment;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class EmploymentException extends DomainException {

    public EmploymentException(EmploymentErrorCode errorCode) {
        super(errorCode);
    }

    public static EmploymentException notFound() {
        return new EmploymentException(EmploymentErrorCode.EMPLOYMENT_NOT_FOUND);
    }

    public static EmploymentException alreadyTerminated() {
        return new EmploymentException(EmploymentErrorCode.EMPLOYMENT_ALREADY_TERMINATED);
    }

    public static EmploymentException notActive() {
        return new EmploymentException(EmploymentErrorCode.EMPLOYMENT_NOT_ACTIVE);
    }

    public static EmploymentException invalidTarget() {
        return new EmploymentException(EmploymentErrorCode.INVALID_EMPLOYMENT_TARGET);
    }

    public static EmploymentException personAlreadyEmployed() {
        return new EmploymentException(EmploymentErrorCode.PERSON_ALREADY_EMPLOYED);
    }
}
