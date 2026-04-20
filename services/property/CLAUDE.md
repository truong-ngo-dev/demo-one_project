# CLAUDE.md — Property Service

## 1. Project Overview

- **Role**: Source of truth cho **tài sản vật lý** (tòa nhà, tầng, căn hộ) và **thỏa thuận chiếm dụng** (OccupancyAgreement) giữa Party với tài sản đó. Không biết về Party internals, RoleContext, hay User/Role.
- **Stack**: Java 21, Spring Boot 4.x, MySQL, Jackson 3.x (`tools.jackson.databind`).
- **Base Package**: `vn.truongngo.apartcom.one.service.property`
- **Architecture**: DDD + CQRS + Hexagonal + Vertical Slice.

---

## 2. Đọc trước khi làm

| Muốn làm gì                           | Đọc file này trước                                                                                    |
|---------------------------------------|-------------------------------------------------------------------------------------------------------|
| **Hiểu cấu trúc code/package**        | [**SERVICE_MAP.md**](SERVICE_MAP.md)                                                                  |
| Hiểu domain model và design decisions | [Party Model Design](../../docs/development/260416_01_design_party_model/00_overview.md)              |
| Hiểu aggregate boundaries và schema   | [Property Service Design](../../docs/development/260416_01_design_party_model/02_property_service.md) |
| Convention package/DDD/CQRS           | [DDD Structure](../../docs/conventions/ddd-structure.md)                                              |
| Convention API/response format        | [API Design](../../docs/conventions/api-design.md)                                                    |
| Convention error handling             | [Error Handling](../../docs/conventions/error-handling.md)                                            |
| Convention testing                    | [Testing](../../docs/conventions/testing.md)                                                          |

---

## 3. Development Commands

```bash
./mvnw clean install     # Build
./mvnw spring-boot:run   # Run
./mvnw test              # Test
```

---

## 4. Domain Boundaries

Property service **owns**: `FixedAsset`, `OccupancyAgreement`

Property service **không biết về**: `Party`, `Person`, `Organization`, `Household`, `RoleContext`, `User`, `Role`

Cross-service interaction:
- **Publish events**: `BuildingCreated`, `UnitCreated`, `OccupancyAgreementActivated`, `OccupancyAgreementTerminated`
- **Không expose internal API** — cross-service data là reference ID (party_id, managing_org_id)
- **Không call sang service khác** — mọi cross-service data là reference ID

---

## 5. Aggregate Boundaries

| Aggregate Root       | Entities bên trong | Ghi chú                                                                          |
|----------------------|--------------------|----------------------------------------------------------------------------------|
| `FixedAsset`         | —                  | Self-referencing qua `parent_id`; materialized path `/bldg/flr/unit`             |
| `OccupancyAgreement` | —                  | Standalone AR; không có Agreement supertype; enforce invariants I1–I5 tại domain |

**FixedAsset types**: `BUILDING`, `FLOOR`, `RESIDENTIAL_UNIT`, `COMMERCIAL_SPACE`, `COMMON_AREA`, `FACILITY`, `MEETING_ROOM`, `PARKING_SLOT`, `EQUIPMENT`

**Agreement lifecycle**: `PENDING → ACTIVE → TERMINATED / EXPIRED`

---

## 6. Business Invariants

| ID | Rule                                                                                             |
|----|--------------------------------------------------------------------------------------------------|
| I1 | Max 1 ACTIVE OWNERSHIP per FixedAsset                                                            |
| I2 | Max 1 ACTIVE LEASE per FixedAsset tại cùng thời điểm                                             |
| I3 | OWNERSHIP và LEASE có thể đồng thời tồn tại trên cùng 1 unit (chủ cho thuê lại)                  |
| I4 | LEASE chỉ được tạo trên RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE                                   |
| I5 | OWNERSHIP chỉ được tạo trên RESIDENTIAL_UNIT                                                     |
| I6 | OWNERSHIP: `end_date` phải null, `party_type` phải là PERSON                                     |
| I7 | LEASE: `end_date` bắt buộc; RESIDENTIAL_UNIT → PERSON/HOUSEHOLD; COMMERCIAL_SPACE → ORGANIZATION |
| I8 | `managing_org_id` bắt buộc trên BUILDING                                                         |

---

## 7. Agent Guardrails

1. **Không import Spring vào domain/** — domain layer phải pure Java.
2. **Không gọi sang service khác** — property-service không có outbound HTTP client.
3. **Jackson 3.x** — dùng `tools.jackson.databind.ObjectMapper`, KHÔNG dùng `com.fasterxml.jackson.databind`.
4. **Invariants I1–I5** phải validate tại domain layer trước khi persist — không để application layer bypass.
5. **managing_org_id** là reference ID thuần — không resolve sang party-service, không validate tồn tại.
6. **Chỉ `→ ACTIVE` và `→ TERMINATED/EXPIRED`** mới emit event — `PENDING` không emit.
7. **`agreementType` phải có trong event payload** — admin-service dùng để phân biệt RoleContext.
