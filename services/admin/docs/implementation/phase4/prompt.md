# Prompt: Admin Service IAM — Phase 4: Event Consumers

**Vai trò**: Bạn là Senior Backend Engineer implement 5 event consumer handlers cho `services/admin`. Các handler này xử lý domain events từ `party-service` và `property-service` để cập nhật RoleContext của User.

> **Thứ tự implement**: Phase 1 → Phase 3 → **Phase 4** → Phase 2 → Phase 5. Phase 1 và Phase 3 phải xong trước.

> **Transport layer**: Cơ chế nhận event (Kafka/RabbitMQ/webhook) là **out of scope**. Implement handlers như plain `@Component` service classes — transport adapter sẽ gọi vào sau. Handlers phải `@Transactional`.

**Yêu cầu**: Phase 1 và Phase 3 đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md
2. Design: @docs/development/260416_01_design_party_model/03_admin_iam.md (Section 4)
3. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 4)
4. Service overview: @services/admin/CLAUDE.md

## Files tham khảo pattern

- Pattern EventHandler: `services/admin/src/main/java/.../infrastructure/cross_cutting/audit/AuditLogEventHandler.java`
- Pattern command handler: `services/admin/src/main/java/.../application/user/assign_roles/AssignRoles.java`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Context từ Phase 1

### RoleContext signatures
```java
// create — orgType nullable
static RoleContext create(Scope scope, String orgId, OrgType orgType, Set<RoleId> roleIds);

// reconstitute
static RoleContext reconstitute(Long id, Scope scope, String orgId, OrgType orgType,
                                Set<RoleId> roleIds, RoleContextStatus status);

// revoke
void revoke();  // throws ROLE_CONTEXT_ALREADY_REVOKED if already REVOKED
```

### Role signatures
```java
// register
static Role register(String name, String description, Scope scope);

// reconstitute
static Role reconstitute(RoleId id, String name, String description, Scope scope, Auditable auditable);
```

### User signatures
```java
// reconstitute — partyId is last param
static User reconstitute(UserId id, String username, String email, String phoneNumber,
                         String fullName, UserPassword password,
                         Set<SocialConnection> socialConnections, UserStatus status,
                         Set<RoleContext> roleContexts, boolean usernameChanged,
                         Instant lockedAt, Instant createdAt, Instant updatedAt,
                         String partyId);

// addRoleContext — orgType nullable
public void addRoleContext(Scope scope, String orgId, OrgType orgType, Set<RoleId> roleIdsForContext);
```

### RoleContextStatus
```java
package vn.truongngo.apartcom.one.service.admin.domain.user;
public enum RoleContextStatus { ACTIVE, REVOKED }
```

### OrgType
```java
package vn.truongngo.apartcom.one.service.admin.domain.user;
public enum OrgType { PARTY, FIXED_ASSET }
```

## Context từ Phase 3

### BuildingReferenceRepository

```java
void upsert(BuildingReference ref);
boolean existsById(String buildingId);
Optional<BuildingReference> findById(String buildingId);
```

### OrgReferenceRepository

```java
void upsert(OrgReference ref);
boolean existsById(String orgId);
```

---

## Nhiệm vụ cụ thể

Package gốc: `application/event/`

---

### Handler 1 — `BuildingCreatedEventHandler`

Package: `application/event/building/`

**Event payload** (từ property-service):
```java
// Tạo local DTO để nhận payload — không import từ property-service
record BuildingCreatedPayload(String buildingId, String name, String managingOrgId) {}
```

**Flow:**
1. Upsert `BuildingReference.of(buildingId, name, managingOrgId)` qua `BuildingReferenceRepository.upsert()`

> Pattern: `@Component`, `@Transactional`. Không phải `EventHandler<E>` từ lib (đó là cho internal events) — đây là plain service class để transport adapter gọi.

---

### Handler 2 — `OrganizationCreatedEventHandler`

Package: `application/event/organization/`

**Event payload**:
```java
record OrganizationCreatedPayload(String partyId, String name, String orgType) {}
```

**Flow:**
1. `IF orgType == "BQL"` → upsert `OrgReference.of(partyId, name, orgType)` qua `OrgReferenceRepository.upsert()`
2. Các orgType khác: bỏ qua (không throw)

---

### Handler 3 — `OccupancyAgreementActivatedEventHandler`

Package: `application/event/agreement/`

**Event payload**:
```java
record OccupancyAgreementActivatedPayload(
    String agreementId,
    String partyId,
    String partyType,   // "PERSON" | "HOUSEHOLD" | "ORGANIZATION"
    String assetId,
    String agreementType  // "OWNERSHIP" | "LEASE"
) {}
```

**Flow** (theo design doc Section 4.3):

```
OWNERSHIP + PERSON  hoặc  LEASE + PERSON:
  → userRepo.findByPartyId(partyId)
  → IF user exists: user.addRoleContext(RESIDENT, assetId, FIXED_ASSET, Set.of())
  → IF not exists: log warning, return (không throw — party chưa register)

LEASE + HOUSEHOLD:
  → partyClient.getMembers(partyId) → List<String> personIds
  → với mỗi personId: userRepo.findByPartyId(personId)
    → IF exists: addRoleContext(RESIDENT, assetId, FIXED_ASSET, Set.of())

LEASE + ORGANIZATION:
  → partyClient.getMembers(partyId) → List<String> personIds
  → personIds[0] (TENANT_ADMIN): userRepo.findByPartyId(personIds[0])
    → IF exists: addRoleContext(TENANT, partyId, PARTY, Set.of())
```

**`PartyClient`** — interface port cần tạo (`domain/party/PartyClient.java`):
```java
public interface PartyClient {
    List<String> getMembers(String partyId);  // trả về danh sách personId
}
```

Implementation trong `infrastructure/adapter/client/PartyClientAdapter.java` — dùng `RestClient` hoặc `WebClient`. Base URL từ config `party-service.base-url`. Endpoint: `GET /internal/parties/{partyId}/members`. Trả về `List<String>`. Nếu call thất bại: log + trả về empty list (graceful degradation).

**Rule B2**: Trước khi `addRoleContext`, kiểm tra `user.getPartyId() != null` — nhưng vì partyId đã được set khi user register/link party, bước này chỉ log warning nếu partyId null.

---

### Handler 4 — `OccupancyAgreementTerminatedEventHandler`

Package: `application/event/agreement/` (cùng package với Handler 3)

**Event payload**:
```java
record OccupancyAgreementTerminatedPayload(
    String agreementId,
    String partyId,
    String partyType,
    String assetId,
    String agreementType
) {}
```

**Flow** (theo design doc Section 4.4):

```
OWNERSHIP  hoặc  LEASE + (PERSON | HOUSEHOLD):
  → userRepo.findAllByActiveRoleContext(RESIDENT, assetId)
  → với mỗi user: user.revokeRoleContext(RESIDENT, assetId)
    (gọi RoleContext.revoke() qua User method)

LEASE + ORGANIZATION:
  → userRepo.findAllByActiveRoleContext(TENANT, partyId)
  → với mỗi user: user.revokeRoleContext(TENANT, partyId)
  → tenantSubRoleRepo.deleteAllByOrgId(partyId)  ← inject sau Phase 2
```

> **Note**: `tenantSubRoleRepo` chưa available ở Phase 4. Inject `Optional<TenantSubRoleAssignmentRepository>` hoặc để TODO comment — implement full sau Phase 2.

**`UserRepository`** cần thêm method**:
```java
List<User> findAllByActiveRoleContext(Scope scope, String orgId);
```
Add method này vào `UserRepository` interface và implement trong adapter.

---

### Handler 5 — `EmploymentTerminatedEventHandler`

Package: `application/event/employment/`

**Event payload**:
```java
record EmploymentTerminatedPayload(String employmentId, String personId, String orgId) {}
```

**Flow**:
1. `userRepo.findByPartyId(personId)` → nếu không tìm thấy: log warning, return
2. Tìm tất cả RoleContext `{scope=OPERATOR}` của user
3. Với mỗi OPERATOR context: check xem building có `managingOrgId == orgId` không qua `BuildingReferenceRepository.findById(context.getOrgId())`
4. Nếu match: `user.revokeRoleContext(OPERATOR, context.getOrgId())`
5. `userRepository.save(user)`

---

## Yêu cầu method mới trên `UserRepository`

Thêm vào `domain/user/UserRepository.java`:
```java
Optional<User> findByPartyId(String partyId);
List<User> findAllByActiveRoleContext(Scope scope, String orgId);
```

Implement trong `UserJpaRepository` (Spring Data derived query hoặc `@Query`):
```java
// findByPartyId: simple derived query
Optional<UserJpaEntity> findByPartyId(String partyId);

// findAllByActiveRoleContext: cần @Query
@Query("SELECT DISTINCT u FROM UserJpaEntity u JOIN u.roleContexts rc " +
       "WHERE rc.scope = :scope AND rc.orgId = :orgId AND rc.status = 'ACTIVE'")
List<UserJpaEntity> findAllByActiveRoleContext(Scope scope, String orgId);
```

---

## Không implement

- Message broker setup (Kafka/RabbitMQ) — transport layer out of scope
- TenantSubRoleAssignment (Phase 2) — chỉ leave TODO comment trong Handler 4
- Auth context endpoint (Phase 5)

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` tất cả items Phase 4

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### PHASE5_CONTEXT BLOCK
- Package paths của tất cả 5 handler classes
- `PartyClient` interface — full package path + method signature
- `UserRepository` — 2 method mới (tên thực tế)
- Deviation so với spec (nếu có)

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
