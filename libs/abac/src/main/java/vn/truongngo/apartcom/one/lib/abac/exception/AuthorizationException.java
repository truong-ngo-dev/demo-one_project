package vn.truongngo.apartcom.one.lib.abac.exception;

import lombok.Getter;

/**
 * Custom exception for authorization-related errors.
 * @author Truong Ngo
 */
@Getter
public class AuthorizationException extends RuntimeException {

    private Object detail;
    private Long timestamp;

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Object detail, Long timestamp) {
        super(message);
        this.detail = detail;
        this.timestamp = timestamp;
    }
}
