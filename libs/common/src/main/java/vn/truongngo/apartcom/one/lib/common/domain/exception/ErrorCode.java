package vn.truongngo.apartcom.one.lib.common.domain.exception;

public interface ErrorCode {
    String code();
    String defaultMessage();
    String messageKey();
    int httpStatus();
}
