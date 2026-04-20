# Prompt: Party Service Phase 3 — Infrastructure Layer (Employment)

**Vai trò**: Bạn là Senior Backend Engineer implement Infrastructure Layer cho aggregate `Employment`. Domain layer (Phase 3 — 3.1) đã xong. Nhiệm vụ: migration SQL, JPA entities, repository, mapper, persistence adapter.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Schema: @docs/development/260416_01_design_party_model/01_party_service.md (Section 3)
3. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 3 — 3.2)

## Files tham khảo pattern (Phase 1 & 2)

- JPA entity với @OneToMany: `infrastructure/persistence/party/PartyJpaEntity.java` (Party → PartyIdentificationJpaEntity)
- JPA entity pattern: `infrastructure/persistence/party_relationship/PartyRelationshipJpaEntity.java`
- JPA repository: `infrastructure/persistence/party_relationship/PartyRelationshipJpaRepository.java`
- Mapper pattern: `infrastructure/persistence/party_relationship/PartyRelationshipMapper.java`
- Adapter (upsert): `infrastructure/adapter/repository/party_relationship/PartyRelationshipPersistenceAdapter.java`
- Migration: `src/main/resources/db/migration/V2__create_party_relationship_tables.sql`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ Phase 3 — 01_domain

```
### Phase 3 Domain — packages thực tế

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Package `domain.employment`

| Class | Ghi chú |
|-------|---------|
| `EmploymentId` | `extends AbstractId<String>` — factory: `of(String)`, `generate()` |
| `EmploymentType` | enum `FULL_TIME`, `PART_TIME`, `CONTRACT` |
| `EmploymentStatus` | enum `ACTIVE`, `TERMINATED` |
| `BQLPosition` | enum `MANAGER`, `DEPUTY_MANAGER`, `FINANCE`, `TECHNICAL`, `SECURITY`, `RECEPTIONIST`, `STAFF` |
| `PositionAssignment` | Entity (không phải AR) — owned bởi Employment |
| `Employment` | AR — extends `AbstractAggregateRoot<EmploymentId>` |
| `EmploymentRepository` | port — extends `Repository<Employment, EmploymentId>` |
| `EmploymentErrorCode` | enum implements `ErrorCode` |
| `EmploymentException` | extends `DomainException` |

#### `Employment` — exact field types

```java
private final EmploymentId           id            // from super
private final PartyRelationshipId    relationshipId // immutable
private final PartyId                employeeId     // immutable
private final PartyId                orgId          // immutable
private final EmploymentType         employmentType // immutable
private       EmploymentStatus       status         // MUTABLE
private       List<PositionAssignment> positions    // MUTABLE — append only
private final LocalDate              startDate      // immutable
private       LocalDate              endDate        // MUTABLE, nullable
```

#### `PositionAssignment` — exact field types

```java
private final UUID        id
private final BQLPosition position
private final String      department  // nullable
private final LocalDate   startDate
private       LocalDate   endDate     // MUTABLE (set khi employment.terminate())
```

Getters: `getId()`, `getPosition()`, `getDepartment()`, `getStartDate()`, `getEndDate()`

#### `EmploymentRepository` — method signatures

```java
Optional<Employment> findById(EmploymentId id)           // từ Repository base
void save(Employment employment)                          // từ Repository base
void delete(EmploymentId id)                             // từ Repository base
List<Employment> findByOrgId(PartyId orgId)
List<Employment> findByEmployeeId(PartyId employeeId)
boolean existsActiveByEmployeeIdAndOrgId(PartyId employeeId, PartyId orgId)
```

#### `EmploymentErrorCode` — full values

```
EMPLOYMENT_NOT_FOUND          ("20201", 404)
EMPLOYMENT_ALREADY_TERMINATED ("20202", 422)
EMPLOYMENT_NOT_ACTIVE         ("20203", 422)
INVALID_EMPLOYMENT_TARGET     ("20204", 422)
PERSON_ALREADY_EMPLOYED       ("20205", 409)
```

#### Domain Events — constructor signatures

```java
EmploymentCreatedEvent(EmploymentId employmentId, PartyId personId, PartyId orgId)
    // aggregateId = employmentId.getValue()
    // String fields: employmentId, personId, orgId

EmploymentTerminatedEvent(EmploymentId employmentId, PartyId personId, PartyId orgId)
    // String fields: employmentId, personId, orgId

PositionAssignedEvent(EmploymentId employmentId, BQLPosition position, String department, LocalDate startDate)
    // aggregateId = employmentId.getValue()
    // String: employmentId, position (BQLPosition.name()), department (nullable); LocalDate: startDate
```

#### Mapper note — mutable vs immutable

- `toEntity()` — map tất cả fields bao gồm positions (lần insert)
- `updateEntity(existing, domain)` — update: `status`, `endDate`, và sync `positions` list
- `toDomain()` — dùng `Employment.reconstitute(...)` với đầy đủ fields kể cả positions
- `PositionAssignment.endDate` là MUTABLE — phải update khi `terminate()` được gọi
```

---

## Nhiệm vụ cụ thể

### 1. Migration — `V3__create_employment_tables.sql`

```sql
CREATE TABLE employment (
    id              VARCHAR(36) NOT NULL,
    relationship_id VARCHAR(36) NOT NULL,
    employee_id     VARCHAR(36) NOT NULL,
    org_id          VARCHAR(36) NOT NULL,
    employment_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT') NOT NULL,
    status          ENUM('ACTIVE', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL,
    end_date        DATE,
    PRIMARY KEY (id),
    UNIQUE KEY uq_employment_relationship (relationship_id),
    CONSTRAINT fk_emp_relationship FOREIGN KEY (relationship_id) REFERENCES party_relationship (id),
    CONSTRAINT fk_emp_employee     FOREIGN KEY (employee_id)     REFERENCES person (party_id),
    CONSTRAINT fk_emp_org          FOREIGN KEY (org_id)          REFERENCES organization (party_id)
);

CREATE TABLE position_assignment (
    id            VARCHAR(36) NOT NULL,
    employment_id VARCHAR(36) NOT NULL,
    position      ENUM('MANAGER', 'DEPUTY_MANAGER', 'FINANCE', 'TECHNICAL',
                       'SECURITY', 'RECEPTIONIST', 'STAFF') NOT NULL,
    department    VARCHAR(100),
    start_date    DATE NOT NULL,
    end_date      DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_pos_employment FOREIGN KEY (employment_id) REFERENCES employment (id)
);
```

### 2. JPA Entities

#### `EmploymentJpaEntity`

Package: `infrastructure/persistence/employment/`

```
@Entity @Table(name = "employment")
@Getter @Setter @NoArgsConstructor

Fields:
    String id                                  (PK — không @GeneratedValue)
    String relationshipId
    String employeeId
    String orgId
    @Enumerated(EnumType.STRING) EmploymentType employmentType
    @Enumerated(EnumType.STRING) EmploymentStatus status
    LocalDate startDate
    LocalDate endDate                           (nullable)
    @OneToMany(mappedBy = "employmentId", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PositionAssignmentJpaEntity> positions
```

#### `PositionAssignmentJpaEntity`

```
@Entity @Table(name = "position_assignment")
@Getter @Setter @NoArgsConstructor

Fields:
    String id                                  (PK)
    String employmentId                        (FK — không @ManyToOne, dùng plain String để tránh lazy load issue)
    @Enumerated(EnumType.STRING) BQLPosition position
    String department                          (nullable)
    LocalDate startDate
    LocalDate endDate                          (nullable)
```

> Dùng `mappedBy = "employmentId"` + plain String field `employmentId` trong `PositionAssignmentJpaEntity` (không phải `@ManyToOne`). Pattern này nhất quán với cách `PartyIdentificationJpaEntity` link về `PartyJpaEntity`.

### 3. JPA Repository — `EmploymentJpaRepository`

```java
public interface EmploymentJpaRepository extends JpaRepository<EmploymentJpaEntity, String> {
    List<EmploymentJpaEntity> findByOrgId(String orgId);
    List<EmploymentJpaEntity> findByEmployeeId(String employeeId);
    boolean existsByEmployeeIdAndOrgIdAndStatus(String employeeId, String orgId, EmploymentStatus status);
}
```

> `PositionAssignmentJpaEntity` KHÔNG cần repository riêng — được quản lý qua cascade từ `EmploymentJpaEntity`.

### 4. Mapper — `EmploymentMapper`

Static class với 3 methods:

- `toDomain(EmploymentJpaEntity)` → `Employment` — dùng `Employment.reconstitute(...)`, map positions list
- `toEntity(Employment)` → `EmploymentJpaEntity` — map tất cả fields + positions
- `updateEntity(EmploymentJpaEntity existing, Employment domain)` → void
  - Update: `existing.setStatus(...)`, `existing.setEndDate(...)`
  - Sync positions: clear existing positions, add all từ domain (đơn giản nhất — positions chỉ thêm mới hoặc update endDate khi terminate)

Helper method riêng (private static):
- `toDomainPosition(PositionAssignmentJpaEntity)` → `PositionAssignment`
- `toEntityPosition(PositionAssignment, String employmentId)` → `PositionAssignmentJpaEntity`

### 5. Adapter — `EmploymentPersistenceAdapter`

Package: `infrastructure/adapter/repository/employment/`

Implements `EmploymentRepository`. Dùng **upsert pattern** (check exists → updateEntity hoặc toEntity).

```java
// existsActiveByEmployeeIdAndOrgId
return jpaRepository.existsByEmployeeIdAndOrgIdAndStatus(
    employeeId.getValue(), orgId.getValue(), EmploymentStatus.ACTIVE);

// findByOrgId
return jpaRepository.findByOrgId(orgId.getValue()).stream()
    .map(EmploymentMapper::toDomain).toList();

// findByEmployeeId
return jpaRepository.findByEmployeeId(employeeId.getValue()).stream()
    .map(EmploymentMapper::toDomain).toList();
```

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`plan.md`** — tick `[x]` tất cả items trong **3.2 Infrastructure layer**
- **`SERVICE_MAP.md`** — cập nhật section **Infrastructure Layer**: thêm Employment persistence classes

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### APPLICATION CONTEXT BLOCK
- Package paths thực tế của tất cả infra classes
- `EmploymentJpaRepository` method names chính xác
- Adapter bean name (`@Component`)
- Xác nhận upsert pattern: fields nào được update trong `updateEntity()`
- Xác nhận positions sync strategy trong `updateEntity()`

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
