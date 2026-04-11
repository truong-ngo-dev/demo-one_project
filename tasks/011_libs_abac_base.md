# Task 011: libs/abac — Base ABAC Engine

## Trạng thái
- [x] Hoàn thành — 2026-04-06

## Mục tiêu
Internalize thư viện `abac-authorization` (demo) vào `libs/abac` trong monorepo.
Đây là nền tảng để implement ABAC policy enforcement (`@PreEnforce`, `@PostEnforce`) và
`AuthorizationContextEngine` (UIElement batch evaluation) trong admin service.

## Definition of Done
- [x] `libs/abac` build thành công (`mvn clean install -DskipTests`)
- [x] Không còn import nào trỏ về `com.nob.authorization`
- [x] `Action.semantic("READ")` tạo được synthetic action không cần `HttpServletRequest`
- [x] `@ResourceMapping(resource="user", action="READ")` compile được
- [x] `AuthorizationAspect` đọc `@ResourceMapping` khi có, fallback URL path khi không có
- [x] Admin service compile được sau khi thêm dependency `libs/abac`

## Tham khảo
- Demo source: `G:/Project/Demo/abac-authorization/`
- Design: `docs/business_analysis/abac_dynamic_authz_design_report.md`
- Phân tích tinh chỉnh: conversation log (3 changes cần áp dụng)

---

## Task 1 — Tạo Maven module `libs/abac`

### Việc cần làm
Tạo `libs/abac/pom.xml` standalone (không có root pom — giống `libs/common`):

```text
groupId:    vn.truongngo.apartcom.one.lib
artifactId: abac
version:    1.0.0
java:       21
```

Dependencies:
- `spring-boot-starter-web` (servlet, HttpServletRequest)
- `spring-boot-starter-aop` (AspectJ cho AuthorizationAspect)
- `vn.truongngo.apartcom.one.lib:common:1.0.0` (JsonUtils, StringUtils, ReflectionUtils...) -- không dùng merge với utils có sẵn
- `lombok` (optional)
- `spring-boot-starter-test` (test scope)

Cấu trúc package gốc: `vn.truongngo.apartcom.one.lib.abac`

```
src/main/java/vn/truongngo/apartcom/one/lib/abac/
├── domain/
├── algorithm/
├── evaluation/
├── context/
├── pdp/
├── pip/
├── pep/
├── rap/
├── servlet/
└── exception/
```

---

## Task 2 — Migrate core engine

**Nguồn**: `G:/Project/Demo/abac-authorization/core/src/main/java/com/nob/authorization/core/`

**Package mapping**: `com.nob.authorization.core.*` → `vn.truongngo.apartcom.one.lib.abac.*`

### Files cần migrate

| Source package | Files                                                                                                                                                                                                                                                                                     |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `domain/`      | `AbstractPolicy`, `PolicySet`, `Policy`, `Rule`, `Expression`, `Principle`                                                                                                                                                                                                                |
| `algorithm/`   | `CombineAlgorithm`, `CombineAlgorithmName`, `CombineAlgorithmFactory`, `DenyOverridesCombineAlgorithm`, `DenyUnlessPermitCombineAlgorithm`, `PermitOverridesCombineAlgorithm`, `PermitUnlessDenyCombineAlgorithm`, `FirstApplicableCombineAlgorithm`, `OnlyOneApplicableCombineAlgorithm` |
| `evaluation/`  | `ExpressionEvaluators`, `PolicyEvaluators`, `EvaluationResult`, `ExpressionResult`, `IndeterminateCause`                                                                                                                                                                                  |
| `context/`     | `Subject`, `Action`, `Resource`, `Environment`, `EvaluationContext`, `HttpRequest`, `Metadata`                                                                                                                                                                                            |
| `pdp/`         | `PdpEngine`, `AuthzRequest`, `AuthzDecision`, `PdpConfiguration`, `DecisionStrategy`                                                                                                                                                                                                      |

### Utils — KHÔNG copy, thay bằng `libs/common`

| Demo util                                     | libs/common equivalent                                                     |
|-----------------------------------------------|----------------------------------------------------------------------------|
| `JsonUtils`                                   | `...lib.common.utils.json.JsonUtils`                                       |
| `StringUtils`                                 | `...lib.common.utils.lang.StringUtils`                                     |
| `ReflectionUtils`                             | `...lib.common.utils.reflect.ReflectionUtils`                              |
| `CollectionUtils`, `NumberUtils`, `DateUtils` | tương tự trong `libs/common`                                               |
| `TypeUtils`, `HttpUtils`                      | kiểm tra libs/common — nếu không có thì giữ lại trong `libs/abac` internal |

---

## Task 3 — Migrate authz-client

**Nguồn**: `G:/Project/Demo/abac-authorization/authz-client/src/main/java/com/nob/authorization/authzclient/`

**Package mapping**: `com.nob.authorization.authzclient.*` → `vn.truongngo.apartcom.one.lib.abac.*`

### Files cần migrate

| Source package | Files                                                                                       |
|----------------|---------------------------------------------------------------------------------------------|
| `pip/`         | `SubjectProvider`, `PolicyProvider`, `EnvironmentProvider`, `PipEngine`                     |
| `pep/`         | `PreEnforce`, `PostEnforce`, `PepEngine`, `AuthorizationAspect`                             |
| `rap/`         | `ResourceAccessPoint`, `ResourceAccessConfig`, `ResourceAccessMetadata`, `ParameterMapping` |
| `servlet/`     | `CacheBodyHttpServletRequest`, `CacheBodyServletInputStream`, `CacheRequestBodyFilter`      |
| `exception/`   | `AuthorizationException`                                                                    |

### ResourceProvider — giữ nguyên comment

`pip/ResourceProvider.java`: migrate sang `libs/abac` nhưng **giữ toàn bộ code trong comment**.
Thêm header:
```java
// Kept for future reference — pull-based resource fetching approach (not currently used)
```

---

## Task 4 — Áp dụng 3 targeted changes

### Change 1: `context/Action.java` — thêm no-arg constructor + static factory

```java
// Thêm vào class Action

public Action() {
    this.attributes = new LinkedHashMap<>();
}

public static Action semantic(String actionName) {
    Action action = new Action();
    action.addAttribute("name", actionName);
    return action;
}
```

Cần cho `AuthorizationContextEngine` tạo synthetic Action khi evaluate UIElement
(không có `HttpServletRequest`).

### Change 2: `rap/ResourceMapping.java` — tạo annotation mới

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceMapping {
    String resource();
    String action();
}
```

Dùng trên controller method cùng với `@PreEnforce`:
```java
@GetMapping("/users/{id}")
@ResourceMapping(resource = "user", action = "READ")
@PreEnforce
public ResponseEntity<UserDetail> getUserById() {}
```

### Change 3: `pep/AuthorizationAspect.java` — đọc `@ResourceMapping` trong `prepareAuthzRequest()`

Update method `prepareAuthzRequest()`:
- Lấy `Method` từ `JoinPoint` (đã có pattern trong `preEnforce`)
- Đọc `@ResourceMapping` annotation từ method đó
- **Nếu có**: set `resource.name = annotation.resource()`, `action.addAttribute("name", annotation.action())`
- **Nếu không có**: fallback — giữ nguyên behavior cũ (extract resource name từ URL path)

---

## Task 5 — Wire vào admin service

### Bước 1: Install libs/abac local
```bash
cd G:/Project/Apartcom/one-project/libs/abac
mvn clean install -DskipTests
```

### Bước 2: Thêm vào `services/admin/pom.xml`
```xml
<dependency>
    <groupId>vn.truongngo.apartcom.one.lib</groupId>
    <artifactId>abac</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Bước 3: Verify compile
```bash
cd G:/Project/Apartcom/one-project/services/admin
mvn clean compile -DskipTests
```

**Chưa cần** implement `SubjectProvider`, `PolicyProvider` trong admin service tại task này —
đó là việc của Phase 1 admin console.
