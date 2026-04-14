package vn.truongngo.apartcom.one.service.admin.domain.abac.resource;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class ResourceException extends DomainException {

    public ResourceException(ResourceErrorCode errorCode) {
        super(errorCode);
    }

    public static ResourceException resourceNotFound() {
        return new ResourceException(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    public static ResourceException resourceNameDuplicate() {
        return new ResourceException(ResourceErrorCode.RESOURCE_NAME_DUPLICATE);
    }

    public static ResourceException resourceInUse() {
        return new ResourceException(ResourceErrorCode.RESOURCE_IN_USE);
    }

    public static ResourceException actionNotFound() {
        return new ResourceException(ResourceErrorCode.ACTION_NOT_FOUND);
    }

    public static ResourceException actionNameDuplicate() {
        return new ResourceException(ResourceErrorCode.ACTION_NAME_DUPLICATE);
    }

    public static ResourceException actionInUse() {
        return new ResourceException(ResourceErrorCode.ACTION_IN_USE);
    }
}
