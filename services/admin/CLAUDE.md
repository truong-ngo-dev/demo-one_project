# CLAUDE.md — Admin Service

## 1. Project Overview

- **Role**: Source of truth cho user identity data — user profile, credentials, roles, social connections. Đồng thời là Admin ABAC Policy Console — quản lý toàn bộ vòng đời policy phân quyền.
- **Stack**: Java 21, Spring Boot 4.x, MySQL, Jackson 3.x (`tools.jackson.databind`).
- **Base Package**: `vn.truongngo.apartcom.one.service.admin`
- **Architecture**: DDD + CQRS + Hexagonal + Vertical Slice.

---

## 2. Đọc trước khi làm

| Muốn làm gì                        | Đọc file này trước                                                         |
|------------------------------------|----------------------------------------------------------------------------|
| **Hiểu cấu trúc code/package**     | [**SERVICE_MAP.md**](SERVICE_MAP.md)                                       |
| Implement bất kỳ use case nào      | [Use Case Index](docs/use-cases/UC-000_index.md)                           |
| Làm việc với User domain           | [Domain: User](docs/domains/user.md)                                       |
| Làm việc với Role domain           | [Domain: Role](docs/domains/role.md)                                       |
| Làm việc với ABAC domain           | [Domain: ABAC](docs/domains/abac.md)                                       |
| Hiểu flow Social Registration      | [Social Registration Flow](docs/flows/001_social_registration_flow.md)     |
| Tra cứu thuật ngữ                  | [Glossary](docs/glossary.md) / [Global Glossary](../../docs/glossary.md)   |

---

## 3. Development Commands

```bash
./mvnw clean install     # Build
./mvnw spring-boot:run   # Run
./mvnw test              # Test
```

---

## 4. Agent Guardrails

1. **[PLANNED] = Không implement** — Verify email, Delete user, MFA. Đọc UC-000_index.md trước khi implement bất kỳ use case nào.
2. **ABAC đã fully implemented** (UC-019 → UC-036) — Resource/Action, PolicySet/Policy/Rule, UIElement, Simulator, Audit Log, Coverage. Xem [docs/domains/abac.md](docs/domains/abac.md).
3. **Building Management (Phase 3.5)** — Quản lý tòa nhà và cấu trúc asset. Tích hợp với `property-service` và dùng `building_reference` làm cache trong admin-service.
4. **Jackson 3.x** — dùng `tools.jackson.databind.ObjectMapper`, KHÔNG dùng `com.fasterxml.jackson.databind` (Spring Boot 4.x break).
5. **Internal endpoints** — không expose ra public, cần auth riêng hoặc network policy.
6. **Out of scope** — không xác thực credentials, không quản lý session.