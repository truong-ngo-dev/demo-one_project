# Prompt: Admin Service IAM — Phase 6.2: Operator Portal Application Layer

**Vai trò**: Bạn là Senior Backend Engineer implement 5 use case handlers cho Operator Portal trong `services/admin`.

> **Thứ tự implement**: Phase 6.1 (domain change) phải xong trước.

**Yêu cầu**: Phase 6.1 đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md
2. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 6.3)
3. UC index: @services/admin/docs/use-cases/UC-000_index.md (UC-041 → UC-045)
4. UC-042 detail: @services/admin/docs/use-cases/UC-042_assign_operator_context.md
5. Service overview: @services/admin/CLAUDE.md

## Files tham khảo pattern

- Pattern command handler: `services/admin/src/main/java/.../application/user/assign_roles/AssignRoles.java`
- Pattern query handler: `services/admin/src/main/java/.../application/auth/get_contexts/GetUserContexts.java`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Context từ các phase trước

### User — methods liên quan

```java
// Phase 6.1 — mới
public void linkPartyId(String partyId);          // throw PARTY_ID_ALREADY_SET nếu đã có

// Phase 1
public void addRoleContext(Scope scope, String orgId, OrgType orgType, Set<RoleId> roleIds);
public void revokeRoleContext(Scope scope, String orgId);
public void assignRoleToContext(Scope scope, String orgId, RoleId roleId);
String getPartyId();                               // nullable
Set<RoleContext> getRoleContexts();
```

### RoleContext — methods liên quan

```java
Scope getScope();
String getOrgId();
OrgType getOrgType();
Set<RoleId> getRoleIds();
RoleContextStatus getStatus();   // ACTIVE | REVOKED
boolean matchesScope(Scope scope, String orgId);
```

### UserRepository — methods liên quan

```java
Optional<User> findById(UserId id);
User save(User user);
List<User> findAllByActiveRoleContext(Scope scope, String orgId);
```

### RoleRepository

```java
Set<Role> findAllByIds(Set<RoleId> ids);
```

### Role

```java
Scope getScope();   // ADMIN | OPERATOR | TENANT | RESIDENT
```

### BuildingReferenceRepository

```java
boolean existsById(String buildingId);
```

### UserErrorCode — codes liên quan (Phase 6.1)

- `PARTY_ID_REQUIRED` (10017, 422)
- `PARTY_ID_ALREADY_SET` (10019, 409)
- `BUILDING_NOT_FOUND` (10020, 404)
- `ROLE_SCOPE_MISMATCH` (10021, 422)
- `ROLE_CONTEXT_NOT_FOUND` (10016, 404)
- `ROLE_CONTEXT_ALREADY_EXISTS` (10015, 409)

---

## Nhiệm vụ cụ thể

Package gốc: `application/operator/`

---

### UC-041 — LinkPartyId (`link_party_id/`)

```
Command: userId (String), partyId (String)
Result:  void
```

**Flow:**
1. `userRepository.findById(UserId.of(userId))` → throw `UserException.notFound()`
2. `user.linkPartyId(partyId)` — domain throw `PARTY_ID_ALREADY_SET` nếu đã có
3. `userRepository.save(user)`

---

### UC-042 — AssignOperatorContext (`assign_operator_context/`)

```
Command: userId (String), buildingId (String), roleIds (List<String>)
Result:  void
```

**Flow:**
1. `userRepository.findById(UserId.of(userId))` → throw `UserException.notFound()`
2. B2: `user.getPartyId() == null` → throw `new DomainException(UserErrorCode.PARTY_ID_REQUIRED)`
3. B3: `buildingReferenceRepository.existsById(buildingId)` → throw `new DomainException(UserErrorCode.BUILDING_NOT_FOUND)` nếu false
4. Convert: `Set<RoleId> roleIdSet = roleIds.stream().map(RoleId::of).collect(toSet())`
5. B6: `roleRepository.findAllByIds(roleIdSet)` → nếu count != roleIds.size() → throw `RoleException.notFound()`
   - Với mỗi role: `role.getScope() != Scope.OPERATOR` → throw `new DomainException(UserErrorCode.ROLE_SCOPE_MISMATCH)`
6. `user.addRoleContext(Scope.OPERATOR, buildingId, OrgType.FIXED_ASSET, roleIdSet)`
7. `userRepository.save(user)`

---

### UC-043 — RevokeOperatorContext (`revoke_operator_context/`)

```
Command: userId (String), buildingId (String)
Result:  void
```

**Flow:**
1. `userRepository.findById(UserId.of(userId))` → throw `UserException.notFound()`
2. `user.revokeRoleContext(Scope.OPERATOR, buildingId)` — domain throw `ROLE_CONTEXT_NOT_FOUND` hoặc `ROLE_CONTEXT_ALREADY_REVOKED`
3. `userRepository.save(user)`

---

### UC-044 — FindOperatorsByBuilding (`find_operators_by_building/`)

```
Query:  buildingId (String)
Result: List<OperatorView>
```

**`OperatorView` record** (khai báo trong cùng class):
```java
record OperatorView(
    String userId,
    String partyId,        // nullable
    String buildingId,
    List<String> roleIds,
    RoleContextStatus status
) {}
```

**Flow:**
1. `userRepository.findAllByActiveRoleContext(Scope.OPERATOR, buildingId)`
2. Với mỗi user: lấy RoleContext `{scope=OPERATOR, orgId=buildingId}`, map sang `OperatorView`

---

### UC-045 — AssignRolesToOperatorContext (`assign_roles_to_operator/`)

```
Command: userId (String), buildingId (String), roleIds (List<String>)
Result:  void
```

> Thay thế toàn bộ roleIds trong context bằng set mới. Khác với UC-042 (tạo mới context) — UC-045 cập nhật context đã tồn tại.

**Flow:**
1. `userRepository.findById(UserId.of(userId))` → throw `UserException.notFound()`
2. Tìm context: `user.getRoleContexts().stream().filter(c -> c.matchesScope(OPERATOR, buildingId)).findFirst()` → throw `ROLE_CONTEXT_NOT_FOUND` nếu không có
3. Check status: context phải `ACTIVE` → throw `ROLE_CONTEXT_ALREADY_REVOKED` nếu REVOKED
4. Convert: `Set<RoleId> roleIdSet`
5. B6: load roles, check scope == OPERATOR (giống UC-042)
6. Clear và re-assign roles:
   ```java
   // Xóa roles cũ, thêm roles mới via User method
   // Dùng user.removeRoleFromContext() + user.assignRoleToContext()
   // HOẶC: dùng approach replace-all nếu User expose method đó
   ```
   > **Gợi ý**: Nếu `User` chưa có `replaceRolesInContext()`, dùng cách:
   > - Copy current roleIds
   > - Remove từng cái cũ: `user.removeRoleFromContext(OPERATOR, buildingId, oldRoleId)`
   > - Add từng cái mới: `user.assignRoleToContext(OPERATOR, buildingId, newRoleId)`
7. `userRepository.save(user)`

---

## Không implement

- Presentation layer (Phase 6.3)
- Migration mới — không cần

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` tất cả items Phase 6.3

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### PHASE6_APP_CONTEXT BLOCK
- Package paths thực tế của tất cả 5 handler classes
- `OperatorView` record — full fields và package path
- Command record fields thực tế của từng use case
- Deviation so với spec nếu có (đặc biệt UC-045 replace logic)

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
