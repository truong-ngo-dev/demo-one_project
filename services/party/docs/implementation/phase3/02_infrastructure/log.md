# Log: Party Service Phase 3 — Infrastructure Layer (Employment)

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS

---

## Files tạo mới

| File | Package |
|------|---------|
| `V3__create_employment_tables.sql` | `src/main/resources/db/migration/` |
| `PositionAssignmentJpaEntity.java` | `infrastructure.persistence.employment` |
| `EmploymentJpaEntity.java` | `infrastructure.persistence.employment` |
| `EmploymentJpaRepository.java` | `infrastructure.persistence.employment` |
| `EmploymentMapper.java` | `infrastructure.persistence.employment` |
| `EmploymentPersistenceAdapter.java` | `infrastructure.adapter.repository.employment` |

## Files cập nhật

- `plan.md` — tick [x] tất cả mục 3.2
- `SERVICE_MAP.md` — Infrastructure Layer thêm Phase 3 classes + migration V3

## Deviation

- `EmploymentJpaEntity` dùng `@JoinColumn(name = "employment_id")` thay vì `mappedBy` như mô tả trong prompt — `mappedBy` yêu cầu `@ManyToOne` ở child side, không dùng được với plain String field. Pattern thực tế nhất quán với `PartyJpaEntity` (cũng dùng `@JoinColumn`).
- `updateEntity()` sync positions bằng `clear()` + `addAll()` — an toàn vì orphanRemoval=true tự xóa positions cũ, JPA insert lại positions mới (bao gồm cả positions đã có với endDate được update).

---

## APPLICATION CONTEXT BLOCK

```
### Phase 3 Infrastructure — packages thực tế

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Adapter bean (inject qua interface)

| Bean class | Implements | Package |
|-----------|-----------|---------|
| `EmploymentPersistenceAdapter` | `EmploymentRepository` | `infrastructure.adapter.repository.employment` |

Inject tại application layer bằng constructor injection (`@RequiredArgsConstructor`) qua interface:
```java
private final EmploymentRepository employmentRepository;
```

#### `EmploymentJpaRepository` — method names chính xác

```java
List<EmploymentJpaEntity> findByOrgId(String orgId)
List<EmploymentJpaEntity> findByEmployeeId(String employeeId)
boolean existsByEmployeeIdAndOrgIdAndStatus(String employeeId, String orgId, EmploymentStatus status)
```

**Lưu ý**: Application layer KHÔNG gọi JpaRepository trực tiếp — gọi qua `EmploymentRepository` port.

#### Domain Repository — method signatures

```java
Optional<Employment> findById(EmploymentId id)
void save(Employment employment)
void delete(EmploymentId id)
List<Employment> findByOrgId(PartyId orgId)
List<Employment> findByEmployeeId(PartyId employeeId)
boolean existsActiveByEmployeeIdAndOrgId(PartyId employeeId, PartyId orgId)
```

#### Upsert pattern (positions sync)

- `save()` với employment đã tồn tại: adapter gọi `updateEntity(existing, domain)`
- `updateEntity` update: `status`, `endDate`, sync positions bằng `clear()` + `addAll()`
- orphanRemoval=true tự handle việc xóa positions cũ trong DB
```
