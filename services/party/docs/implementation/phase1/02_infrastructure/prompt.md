# Prompt: Party Service Phase 1 — Infrastructure Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Infrastructure Layer cho `services/party`. Domain layer đã xong (01_domain). Nhiệm vụ này: migration SQL + JPA entities + persistence adapters.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/party/CLAUDE.md
3. Schema chi tiết: @docs/development/260416_01_design_party_model/01_party_service.md (Section 3)
4. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 1 — 1.2 Infrastructure layer)

## Files tham khảo pattern (từ services/admin)

- Pattern JPA entity: `services/admin/src/main/java/.../infrastructure/persistence/role/RoleJpaEntity.java`
- Pattern mapper: `services/admin/src/main/java/.../infrastructure/persistence/role/RoleMapper.java`
- Pattern persistence adapter: `services/admin/src/main/java/.../infrastructure/adapter/repository/role/RolePersistenceAdapter.java`
- Pattern migration: `services/admin/src/main/resources/db/migration/V1__init_schema.sql`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ 01_domain (paste INFRASTRUCTURE CONTEXT BLOCK từ handoff vào đây)

[INFRASTRUCTURE CONTEXT BLOCK]

---

## Nhiệm vụ cụ thể

### 1. Flyway Migration

File: `services/party/src/main/resources/db/migration/V1__create_party_tables.sql`

Tạo đúng thứ tự (FK dependencies):
```
party → person, organization, household → party_relationship (Phase 2, SKIP)
party_identification → party
```

Schema đầy đủ xem Section 3 trong `01_party_service.md`. Phase 1 chỉ tạo: `party`, `person`, `organization`, `household`, `party_identification`. **Không tạo** `party_relationship`, `employment`, `position_assignment`.

### 2. JPA Entities

Package: `infrastructure/persistence/party/`

- `PartyJpaEntity.java` — `@Entity @Table(name = "party")`, fields map đúng columns, `@OneToMany(cascade=ALL, orphanRemoval=true)` cho `identifications`
- `PartyIdentificationJpaEntity.java` — `@Entity @Table(name = "party_identification")`, `@ManyToOne` back to PartyJpaEntity
- `PersonJpaEntity.java` — `@Entity @Table(name = "person")`, `@Id` là `party_id` (String, không generate)
- `OrganizationJpaEntity.java` — `@Entity @Table(name = "organization")`, `@Id` là `party_id`
- `HouseholdJpaEntity.java` — `@Entity @Table(name = "household")`, `@Id` là `party_id`

Dùng `@Enumerated(EnumType.STRING)` cho tất cả enum fields.

### 3. JPA Repositories (Spring Data)

Package: `infrastructure/persistence/party/`

- `PartyJpaRepository extends JpaRepository<PartyJpaEntity, String>`
  ```java
  boolean existsByIdentificationsTypeAndIdentificationsValue(
      PartyIdentificationType type, String value);
  Page<PartyJpaEntity> findByTypeAndNameContainingIgnoreCaseAndStatus(..., Pageable pageable);
  ```
- `PersonJpaRepository extends JpaRepository<PersonJpaEntity, String>`
- `OrganizationJpaRepository extends JpaRepository<OrganizationJpaEntity, String>`
- `HouseholdJpaRepository extends JpaRepository<HouseholdJpaEntity, String>`

### 4. Mappers

Package: `infrastructure/persistence/party/`

- `PartyMapper.java` — toDomain(PartyJpaEntity) → Party, toEntity(Party) → PartyJpaEntity; tương tự cho PartyIdentification
- `PersonMapper.java` — toDomain(PersonJpaEntity) → Person, toEntity(Person) → PersonJpaEntity
- `OrganizationMapper.java`
- `HouseholdMapper.java`

Dùng method tĩnh hoặc Spring `@Component` — follow pattern của admin service.

### 5. Persistence Adapters

Package: `infrastructure/adapter/repository/party/`

- `PartyPersistenceAdapter.java` — implements `PartyRepository` (domain port)
  - `existsByIdentification()`: delegate sang `PartyJpaRepository.existsByIdentificationsTypeAndIdentificationsValue()`
- `PersonPersistenceAdapter.java` — implements `PersonRepository`
- `OrganizationPersistenceAdapter.java` — implements `OrganizationRepository`
- `HouseholdPersistenceAdapter.java` — implements `HouseholdRepository`

**Không implement**: Application layer, Presentation layer, party_relationship / employment tables.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/plan.md`** — tick `[x]` tất cả items trong mục **1.2 Infrastructure layer**
- **`services/party/SERVICE_MAP.md`** — cập nhật section **Infrastructure Layer**: điền tên các adapter/JPA entity đã tạo, xóa "chưa có implementation nào"

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cung cấp:

### APPLICATION CONTEXT BLOCK
- Package paths thực tế của tất cả files đã tạo
- Query method names thực tế trên JPA repositories (để application layer biết inject gì)
- Deviation so với spec (nếu có)
- Lưu ý transaction: adapter nào cần `@Transactional`, adapter nào để application layer quản lý
---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
