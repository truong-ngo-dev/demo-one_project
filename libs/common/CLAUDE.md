# libs/common

Thư viện dùng chung cho tất cả services. Chứa base classes, interfaces, và utilities.
**Không implement lại những gì đã có ở đây.**

Base package: `vn.truongngo.apartcom.one.lib.common`

---

## Những gì có sẵn — dùng thay vì tự implement

### Domain Model (`domain/model/`)

| Class/Interface         | Dùng khi                                                                                                                                          |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `AbstractAggregateRoot` | Extend cho mọi Aggregate Root — có sẵn `pullEvents()`, `registerEvent()`                                                                          |
| `AbstractDomainEvent`   | Extend cho mọi Domain Event                                                                                                                       |
| `AbstractId`            | Extend cho mọi Typed ID (ví dụ: `UserId extends AbstractId`)                                                                                      |
| `Auditable`             | Value Object — nhúng vào aggregate cần audit (`createdAt`, `updatedAt`, `createdBy`, `updatedBy` — kiểu `Long` cho timestamp, `String` cho actor) |
| `AggregateRoot`         | Marker interface                                                                                                                                  |
| `Entity`                | Marker interface                                                                                                                                  |
| `ValueObject`           | Marker interface                                                                                                                                  |
| `DomainEvent`           | Marker interface                                                                                                                                  |
| `Id`                    | Marker interface                                                                                                                                  |

```text
// ✅ Extend AbstractAggregateRoot
public class User extends AbstractAggregateRoot<UserId> { ... }

// ✅ Extend AbstractId
public class UserId extends AbstractId { ... }

// ✅ Extend AbstractDomainEvent
public class UserCreatedEvent extends AbstractDomainEvent { ... }

// ❌ Không tự implement pullEvents(), registerEvent()
```

---

### Exception (`domain/exception/`)

| Class/Interface   | Mô tả                                              |
|-------------------|----------------------------------------------------|
| `DomainException` | Base exception — nhận `ErrorCode`                  |
| `ErrorCode`       | Interface — implement bằng enum trong từng service |

→ Chi tiết cách dùng: [`../../docs/conventions/error-handling.md`](../../docs/conventions/error-handling.md)

---

### Application (`application/`)

| Interface              | Dùng khi                                         |
|------------------------|--------------------------------------------------|
| `CommandHandler<C, R>` | Implement cho mọi Command Handler                |
| `QueryHandler<Q, R>`   | Implement cho mọi Query Handler                  |
| `EventDispatcher`      | Inject để dispatch domain events sau khi persist |

```text
// ✅ Implement CommandHandler
public class RegisterUserHandler implements CommandHandler<RegisterUserCommand> {
    @Override
    public void handle(RegisterUserCommand command) { ... }
}

// ✅ Implement QueryHandler
public class FindUserByIdHandler implements QueryHandler<FindUserByIdQuery, UserResponse> {
    @Override
    public UserResponse handle(FindUserByIdQuery query) { ... }
}
```

---

### Domain Service (`domain/service/`)

| Interface           | Dùng khi                                     |
|---------------------|----------------------------------------------|
| `EventHandler<E>`   | Implement cho mọi Event Handler              |
| `Repository<T, ID>` | Exrdtend cho mọi domain Repository interface |

```text
// ✅ Extend Repository
public interface UserRepository extends Repository<User, UserId> { ... }

// ✅ Implement EventHandler
public class UserCreatedHandler implements EventHandler<UserCreatedEvent> { ... }
```

---

### Request Context (`domain/context/`)

| Class                  | Mô tả                                                           |
|------------------------|-----------------------------------------------------------------|
| `RequestContext`       | Lưu state xuyên suốt 1 request: `userId`, `roles`, `traceId`... |
| `RequestContextHolder` | Static accessor để lấy `RequestContext` từ bất kỳ đâu           |

```java
// Lấy thông tin user hiện tại từ bất kỳ layer nào
RequestContext ctx = RequestContextHolder.getContext();
UserId currentUserId = ctx.getUserId();
```

> `RequestContextFilter` (infrastructure) tự động populate context ở đầu mỗi request.
> Không cần khởi tạo thủ công trong business code.

---

### Infrastructure (`infrastructure/`)

| Class                  | Mô tả                                                     |
|------------------------|-----------------------------------------------------------|
| `RequestContextFilter` | Filter tự động khởi tạo `RequestContext` khi nhận request |

Đăng ký filter trong service:
```java
@Bean
public RequestContextFilter requestContextFilter() {
    return new RequestContextFilter();
}
```

---

### Utils (`utils/`)

| Class             | Mô tả                                                 |
|-------------------|-------------------------------------------------------|
| `Assert`          | Precondition checks, throw `DomainException` nếu fail |
| `DateUtils`       | Xử lý date/time                                       |
| `NumberUtils`     | Xử lý số                                              |
| `ObjectUtils`     | Null checks, object utilities                         |
| `AnnotationUtils` | Reflection trên annotation                            |
| `BeanUtils`       | Bean utilities                                        |
| `ClassUtils`      | Reflection trên class                                 |
| `GenericUtils`    | Generic type resolution                               |
| `ReflectionUtils` | General reflection                                    |
| `ConverterUtils`  | Type conversion                                       |
| `DataTypeUtils`   | Data type utilities                                   |
| `JsonUtils`       | Serialize/deserialize JSON                            |

```text
// ✅ Dùng Assert thay vì tự throw
Assert.notNull(userId, UserErrorCode.USER_NOT_FOUND);
Assert.isTrue(user.isActive(), UserErrorCode.ACCOUNT_LOCKED);
```

---

## Quy tắc

1. **Không implement lại** những gì đã có trong lib này
2. **Không sửa** lib này khi đang làm việc trong service — đây là shared code
3. `RequestContext` chỉ đọc trong business code — không tự set thủ công
4. Dùng `Assert` từ lib thay vì `if/throw` thủ công