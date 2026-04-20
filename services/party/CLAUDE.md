# CLAUDE.md — Party Service

## 1. Project Overview

- **Role**: Source of truth cho **identity của các tác nhân** trong hệ thống — ai là ai, quan hệ giữa họ, thông tin định danh pháp lý, và quan hệ nhân sự BQL. Không biết về FixedAsset, RoleContext, hay Agreement.
- **Stack**: Java 21, Spring Boot 4.x, MySQL, Jackson 3.x (`tools.jackson.databind`).
- **Base Package**: `vn.truongngo.apartcom.one.service.party`
- **Architecture**: DDD + CQRS + Hexagonal + Vertical Slice.

---

## 2. Đọc trước khi làm

| Muốn làm gì                              | Đọc file này trước                                                                     |
|------------------------------------------|----------------------------------------------------------------------------------------|
| **Hiểu cấu trúc code/package**           | [**SERVICE_MAP.md**](SERVICE_MAP.md)                                                   |
| Hiểu domain model và design decisions    | [Party Model Design](../../docs/development/260416_01_design_party_model/00_overview.md) |
| Hiểu aggregate boundaries và schema      | [Party Service Design](../../docs/development/260416_01_design_party_model/01_party_service.md) |
| Convention package/DDD/CQRS              | [DDD Structure](../../docs/conventions/ddd-structure.md)                               |
| Convention API/response format           | [API Design](../../docs/conventions/api-design.md)                                     |
| Convention error handling                | [Error Handling](../../docs/conventions/error-handling.md)                             |
| Convention testing                       | [Testing](../../docs/conventions/testing.md)                                           |

---

## 3. Development Commands

```bash
./mvnw clean install     # Build
./mvnw spring-boot:run   # Run
./mvnw test              # Test
```

---

## 4. Domain Boundaries

Party service **owns**: `Party`, `Person`, `Organization`, `Household`, `PartyRelationship`, `Employment`, `PositionAssignment`, `PartyIdentification`

Party service **không biết về**: `FixedAsset`, `OccupancyAgreement`, `RoleContext`, `User`, `Role`

Cross-service interaction:
- **Publish events**: `PersonCreated`, `OrganizationCreated`, `HouseholdCreated`, `MemberAdded`, `MemberRemoved`, `EmploymentCreated`, `EmploymentTerminated`, `PositionAssigned`
- **Expose internal API**: `GET /internal/parties/{id}`, `GET /internal/parties/{id}/members`
- **Không call sang service khác** — mọi cross-service data là reference ID

---

## 5. Aggregate Boundaries

| Aggregate Root      | Entities bên trong   | Ghi chú                                                        |
|---------------------|----------------------|----------------------------------------------------------------|
| `Party`             | `PartyIdentification`| Core identity, owns identification lifecycle                   |
| `Person`            | —                    | Share ID với Party (composition pattern), AR riêng             |
| `Organization`      | —                    | Share ID với Party (composition pattern), AR riêng             |
| `Household`         | —                    | Share ID với Party (composition pattern), AR riêng             |
| `PartyRelationship` | —                    | Thin AR, chỉ track kết nối                                     |
| `Employment`        | `PositionAssignment` | AR riêng, link sang PartyRelationship qua FK                   |

`CreatePerson` / `CreateOrganization` / `CreateHousehold` tạo cả `Party` lẫn subtype **atomic tại application layer** (cùng transaction).

---

## 6. Agent Guardrails

1. **Phase 1 scope only** — Party + Person + Organization + Household + PartyIdentification. Phase 2 (PartyRelationship), Phase 3 (Employment) chưa implement — không tự ý thêm.
2. **Không import Spring vào domain/** — domain layer phải pure Java.
3. **Không gọi sang service khác** — party-service không có outbound HTTP client.
4. **Jackson 3.x** — dùng `tools.jackson.databind.ObjectMapper`, KHÔNG dùng `com.fasterxml.jackson.databind`.
5. **Cross-aggregate creation** — `CreatePerson` phải tạo Party + Person trong cùng 1 transaction tại application layer, không phải domain layer.
6. **Internal endpoints** — không expose ra public gateway.
