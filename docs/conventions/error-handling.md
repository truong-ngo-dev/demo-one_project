# Error Handling Convention

> **Dành cho AI agents**: Áp dụng cho tất cả services.
> `ErrorCode`, `DomainException`, `GlobalExceptionHandler` base đều từ `libs/common` —
> không tự định nghĩa lại trong service.

---

## Phân tầng xử lý lỗi

```
domain/          → throw DomainException(ErrorCode)
application/     → let DomainException propagate, hoặc wrap nếu cần
infrastructure/  → wrap technical exception thành DomainException nếu có nghĩa vụ
presentation/    → catch tất cả, map sang HTTP response qua GlobalExceptionHandler
```

---

## ErrorCode convention

`ErrorCode` là interface từ `libs/common` — các service implement bằng enum, đặt trong `domain/{aggregate}/`:

```java
// libs/common — không tự định nghĩa lại trong service
public interface ErrorCode {
    String code();           // mã lỗi, dùng trong response
    String defaultMessage(); // message mặc định nếu không có i18n
    String messageKey();     // key để resolve i18n message
    int httpStatus();        // HTTP status tương ứng
}
```

```java
// domain/user/UserErrorCode.java
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", "error.user.not_found", 404),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "User already exists", "error.user.already_exists", 409),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Account is locked", "error.user.account_locked", 423);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    UserErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    @Override public String code() { return code; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public String messageKey() { return messageKey; }
    @Override public int httpStatus() { return httpStatus; }
}
```

**Quy tắc:**
- `httpStatus` đặt trong ErrorCode — presentation layer không tự quyết định
- `messageKey` chuẩn bị sẵn cho i18n dù chưa dùng ngay
- Mỗi aggregate có 1 enum riêng — không gộp chung tất cả vào 1 enum

---

## DomainException

Từ `libs/common`, nhận `ErrorCode` interface — không cần biết enum cụ thể:

```java
// libs/common
public class DomainException extends RuntimeException {
    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
```

Aggregate-specific exception với factory methods (đặt trong `domain/{aggregate}/`):

```java
// domain/user/UserException.java
public class UserException extends DomainException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }

    public static UserException notFound() {
        return new UserException(UserErrorCode.USER_NOT_FOUND);
    }

    public static UserException alreadyExists() {
        return new UserException(UserErrorCode.USER_ALREADY_EXISTS);
    }

    public static UserException locked() {
        return new UserException(UserErrorCode.ACCOUNT_LOCKED);
    }
}
```

---

## Cách throw trong domain

```text
// ✅ Dùng factory method — rõ ràng, không lặp lại ErrorCode
throw UserException.notFound();

// ✅ Inline nếu chưa có factory method
throw new DomainException(UserErrorCode.USER_NOT_FOUND);

// ❌ Không throw raw exception
throw new RuntimeException("User not found");

// ❌ Không tự truyền httpStatus — đã có trong ErrorCode
throw new DomainException(UserErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
```

---

## Global Exception Handler (presentation layer)

Đọc trực tiếp từ `ErrorCode` interface — không cần biết enum cụ thể là gì:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
            .status(errorCode.httpStatus())
            .body(new ErrorResponse(errorCode.code(), errorCode.defaultMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation() {  }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral() {  }
}
```

---

## ErrorCode naming

```
{AGGREGATE}_{ACTION/STATE}

✅ USER_NOT_FOUND
✅ USER_ALREADY_EXISTS
✅ ROLE_NOT_FOUND
✅ INVALID_CREDENTIALS
✅ ACCOUNT_LOCKED

❌ NOT_FOUND       (quá chung chung)
❌ ERROR_001       (không có nghĩa)
```