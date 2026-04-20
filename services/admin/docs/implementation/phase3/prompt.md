# Prompt: Admin Service IAM — Phase 3: Reference Cache

**Vai trò**: Bạn là Senior Backend Engineer implement Reference Cache cho `services/admin`. Cache này lưu data từ các service khác để validate mà không cần sync call.

> **Thứ tự implement**: Phase 1 → **Phase 3** → Phase 4 → Phase 2 → Phase 5. Phase này phải làm trước Phase 4 (event consumers dùng cache này).

**Yêu cầu**: Phase 1 đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md
2. Design: @docs/development/260416_01_design_party_model/03_admin_iam.md (Section 2.5 và 4.1–4.2)
3. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 3)
4. Service overview: @services/admin/CLAUDE.md

## Files tham khảo pattern

- Pattern simple JPA entity (không phải AR): `services/admin/src/main/java/.../domain/abac/audit/AbacAuditLog.java`
- Pattern persistence adapter: `services/admin/src/main/java/.../infrastructure/persistence/role/`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Nhiệm vụ cụ thể

### 1. Migration V11

File: `src/main/resources/db/migration/V11__reference_cache.sql`

```sql
CREATE TABLE building_reference (
    building_id     VARCHAR(36) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    managing_org_id VARCHAR(36),
    cached_at       DATETIME NOT NULL
);

CREATE TABLE org_reference (
    org_id    VARCHAR(36) PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    org_type  VARCHAR(20) NOT NULL,
    cached_at DATETIME NOT NULL
);
```

> Note: V10 dành cho Phase 2 (TenantSubRoleAssignment). Dùng V11 để giữ thứ tự migration đúng.

### 2. Domain layer (`domain/reference/`)

**`BuildingReference.java`** — plain data holder (không phải AR, không extends AbstractAggregateRoot):
```java
// Fields:
String buildingId;  // PK
String name;
String managingOrgId;   // nullable
Instant cachedAt;

// Factory:
static BuildingReference of(String buildingId, String name, String managingOrgId);
// static reconstitute(...)
```

**`BuildingReferenceRepository.java`** — port interface:
```java
void upsert(BuildingReference ref);
boolean existsById(String buildingId);
Optional<BuildingReference> findById(String buildingId);
```

**`OrgReference.java`** — plain data holder:
```java
// Fields:
String orgId;    // PK
String name;
String orgType;
Instant cachedAt;

// Factory:
static OrgReference of(String orgId, String name, String orgType);
// static reconstitute(...)
```

**`OrgReferenceRepository.java`** — port interface:
```java
void upsert(OrgReference ref);
boolean existsById(String orgId);
```

### 3. Infrastructure layer

Package `infrastructure/persistence/reference/`:

**`BuildingReferenceJpaEntity.java`** — `@Entity @Table(name="building_reference")`, `@Id` là String.

**`BuildingReferenceJpaRepository.java`** — extends `JpaRepository<BuildingReferenceJpaEntity, String>`.

**`BuildingReferenceMapper.java`** — static: `toDomain`, `toEntity`.

Package `infrastructure/adapter/repository/reference/`:

**`BuildingReferencePersistenceAdapter.java`** — implements `BuildingReferenceRepository`:
- `upsert`: `jpaRepo.save(toEntity(ref))` — JPA `save` đã là upsert khi @Id đã biết.
- `existsById`: `jpaRepo.existsById(buildingId)`
- `findById`: `jpaRepo.findById(id).map(toDomain)`

Tương tự cho `OrgReference*`.

---

## Không implement

- Event consumer handlers (Phase 4)
- Không cần controller — reference cache là internal, chỉ được dùng bởi event handlers và validators

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` tất cả items Phase 3

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### PHASE4_CONTEXT BLOCK
- Package paths thực tế của `BuildingReferenceRepository` và `OrgReferenceRepository`
- Full method signatures của 2 repository interfaces
- `BuildingReference.of()` và `OrgReference.of()` — full signatures

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
