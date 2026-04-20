# Prompt: Admin Service IAM — Phase 5: Auth Context Query

**Vai trò**: Bạn là Senior Backend Engineer implement endpoint query RoleContexts cho user trong `services/admin`.

> **Thứ tự implement**: Phase 1 → Phase 3 → Phase 4 → Phase 2 → **Phase 5**. Đây là phase cuối.

**Yêu cầu**: Tất cả phase trước đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md, @docs/conventions/api-design.md
2. Design: @docs/development/260416_01_design_party_model/03_admin_iam.md (Section 5 — Auth flow)
3. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 5)
4. Service overview: @services/admin/CLAUDE.md

## Files tham khảo pattern

- Pattern query handler: `services/admin/src/main/java/.../application/user/find_by_id/FindUserById.java`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Context từ các phase trước

### User + RoleContext

```java
// User
Set<RoleContext> getRoleContexts();
String getPartyId();   // nullable

// RoleContext
Scope getScope();
String getOrgId();
OrgType getOrgType();  // nullable
Set<RoleId> getRoleIds();
RoleContextStatus getStatus();  // ACTIVE | REVOKED
Long getId();
```

### BuildingReferenceRepository

```java
Optional<BuildingReference> findById(String buildingId);
```

### OrgReferenceRepository

```java
boolean existsById(String orgId);
// thêm: Optional<OrgReference> findById(String orgId)  — nếu chưa có, implement thêm
```

---

## Nhiệm vụ cụ thể

### 1. Application layer (`application/auth/`)

**`GetUserContexts.java`**:

```
Query:  userId (String)
Result: List<ContextView>
```

**`ContextView` record**:
```java
record ContextView(
    Long contextId,
    Scope scope,
    String orgId,           // null cho ADMIN
    OrgType orgType,        // null cho ADMIN
    String displayName,     // resolve từ reference cache
    List<String> roleIds    // từ RoleContext.getRoleIds()
) {}
```

**Flow:**
1. Load `user = userRepo.findById(UserId.of(userId))` → throw `UserException.notFound()`
2. Filter `user.getRoleContexts()` → chỉ lấy `status == ACTIVE`
3. Với mỗi active context, resolve `displayName`:
   - `scope == ADMIN` → displayName = `"Admin Portal"`
   - `scope == OPERATOR` → `buildingRefRepo.findById(orgId).map(BuildingReference::getName).orElse(orgId)`
   - `scope == RESIDENT` → `buildingRefRepo.findById(orgId).map(BuildingReference::getName).orElse(orgId)`
   - `scope == TENANT` → `orgRefRepo.findById(orgId).map(OrgReference::getName).orElse(orgId)`
4. Map sang `ContextView` list

### 2. Presentation layer (`presentation/auth/`)

**`AuthContextController.java`** — base path `/api/v1/auth`:

| Method | Path                    | Handler                   | Params                | Response                          |
|--------|-------------------------|---------------------------|-----------------------|-----------------------------------|
| `GET`  | `/api/v1/auth/contexts` | `GetUserContexts.Handler` | `?userId=` (required) | `200 { data: List<ContextView> }` |

> **Note**: Token issuance khi switch context (SwitchContext) thuộc trách nhiệm `oauth2-service` — không implement ở đây.

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` Phase 5; Phase 5 Status → `[x] Completed`

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
