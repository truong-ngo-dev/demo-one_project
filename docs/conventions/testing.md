# Testing Convention

> **Dành cho AI agents**: Áp dụng cho tất cả services.
> Mỗi layer có chiến lược test khác nhau — không dùng `@SpringBootTest` cho unit test domain.

---

## Stack

| Thư viện         | Mục đích                                            |
|------------------|-----------------------------------------------------|
| JUnit 5          | Test runner                                         |
| Mockito          | Mock dependencies trong unit test                   |
| AssertJ          | Fluent assertions                                   |
| H2               | In-memory DB cho integration test persistence layer |
| Spring Boot Test | Test slice cho persistence layer (`@DataJpaTest`)   |

---

## Chiến lược test theo layer

```
domain/          → Unit test thuần — không cần Spring, không cần mock DB
application/     → Unit test — mock repository và dependencies
infrastructure/  → Integration test — H2 + @DataJpaTest
presentation/    → Unit test — mock application handler
```

---

## `domain/` — Unit Test

Không load Spring context, không mock gì ngoài collaborator của aggregate:

```java
// ✅ Pure unit test — nhanh, không dependency
class UserTest {

    @Test
    void register_shouldCreateUserWithPendingStatus() {
        // given
        UserId id = new UserId(UUID.randomUUID());
        Email email = new Email("test@example.com");

        // when
        User user = User.register(id, email);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(user.pullEvents()).hasSize(1);
        assertThat(user.pullEvents().get(0)).isInstanceOf(UserCreatedEvent.class);
    }

    @Test
    void lock_shouldThrowException_whenAlreadyLocked() {
        // given
        User user = UserFixture.lockedUser();

        // when & then
        assertThatThrownBy(user::lock)
            .isInstanceOf(DomainException.class)
            .extracting(e -> ((DomainException) e).getErrorCode())
            .isEqualTo(UserErrorCode.ACCOUNT_LOCKED);
    }
}
```

---

## `application/` — Unit Test với Mock

Mock tất cả dependencies — chỉ test orchestration logic của handler:

```java
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RegisterUserHandlerTest {

    @Mock
    UserRepository userRepository;
    @Mock
    EventDispatcher eventDispatcher;
    @InjectMocks
    RegisterUserHandler handler;

    @Test
    void handle_shouldSaveUserAndDispatchEvent() {
        // given
        RegisterUserCommand command = new RegisterUserCommand();
        when(userRepository.existsByEmail(any())).thenReturn(false);

        // when
        handler.handle(command);

        // then
        verify(userRepository).save(any(User.class));
        verify(eventDispatcher).dispatchAll(anyList());
    }

    @Test
    void handle_shouldThrow_whenEmailAlreadyExists() {
        // given
        RegisterUserCommand command = new RegisterUserCommand(List.of(params));
        when(userRepository.existsByEmail(any())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(UserException.class)
                .extracting(e -> ((DomainException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_ALREADY_EXISTS);
    }
}
```

---

## `infrastructure/persistence/` — Integration Test

Dùng `@DataJpaTest` với H2 — chỉ load persistence layer, không load toàn bộ context:

```java
@DataJpaTest
class UserRepositoryImplTest {

    @Autowired UserJpaRepository jpaRepository;
    UserRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepositoryImpl(jpaRepository, new UserMapper());
    }

    @Test
    void save_shouldPersistUser() {
        // given
        User user = UserFixture.activeUser();

        // when
        repository.save(user);

        // then
        Optional<User> found = repository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
    }
}
```

---

## `presentation/` — Unit Test

Mock application handler — chỉ test request mapping và response format:

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean RegisterUserHandler registerUserHandler;

    @Test
    void register_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "test@example.com" }
                """))
            .andExpect(status().isCreated());
    }

    @Test
    void register_shouldReturn409_whenUserAlreadyExists() throws Exception {
        doThrow(UserException.alreadyExists())
            .when(registerUserHandler).handle(any());

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "test@example.com" }
                """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("USER_ALREADY_EXISTS"));
    }
}
```

---

## Fixture convention

Tạo class `{Aggregate}Fixture` trong `src/test/java` để tái sử dụng test data:

```java
// src/test/java/.../domain/user/UserFixture.java
public class UserFixture {

    public static User activeUser() {
        return User.register(
            new UserId(UUID.randomUUID()),
            new Email("active@example.com")
            // các tham số khác    
        );
    }

    public static User lockedUser() {
        User user = activeUser();
        user.lock();
        user.pullEvents(); // clear events
        return user;
    }
}
```

---

## Naming convention

```
{ClassUnderTest}Test.java

✅ UserTest.java
✅ RegisterUserHandlerTest.java
✅ UserRepositoryImplTest.java
✅ UserControllerTest.java
```

Test method:

```
{method}_{condition}_{expectedResult}

✅ register_shouldCreateUserWithPendingStatus
✅ handle_shouldThrow_whenEmailAlreadyExists
✅ save_shouldPersistUser
```

---

## Quy tắc

1. `domain/` test — không được dùng `@SpringBootTest`, `@ExtendWith(SpringExtension.class)`
2. `application/` test — chỉ dùng `@ExtendWith(MockitoExtension.class)`
3. `infrastructure/` test — dùng `@DataJpaTest`, không dùng `@SpringBootTest`
4. Không test private method — nếu cần test, đó là dấu hiệu cần extract ra class riêng
5. Mỗi test chỉ assert 1 behavior — không nhồi nhiều assertion không liên quan
6. Dùng `UserFixture` thay vì tạo object inline lặp đi lặp lại