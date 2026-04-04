# CLAUDE.md — Admin Service

## 1. Project Overview

- **Role**: Source of truth cho user identity data — user profile, credentials, roles, social connections.
- **Stack**: Java 21, Spring Boot 4.x (TBD — confirm trước khi generate boilerplate), MySQL.
- **Base Package**: `vn.truongngo.apartcom.one.service.admin`
- **Architecture**: DDD + CQRS + Hexagonal + Vertical Slice.

---

## 2. Đọc trước khi làm

| Muốn làm gì                        | Đọc file này trước                                                       |
|------------------------------------|--------------------------------------------------------------------------|
| Implement bất kỳ use case nào      | [Use Case Index](docs/use-cases/UC-000_index.md)                         |
| Làm việc với User domain           | [Domain: User](docs/domains/user.md)                                     |
| Làm việc với Role domain           | [Domain: Role](docs/domains/role.md)                                     |
| Hiểu flow Social Registration      | [Social Registration Flow](docs/flows/001_social_registration_flow.md)   |
| Tra cứu thuật ngữ                  | [Glossary](docs/glossary.md) / [Global Glossary](../../docs/glossary.md) |

---

## 3. Development Commands

```bash
./mvnw clean install     # Build
./mvnw spring-boot:run   # Run
./mvnw test              # Test
```

---

## 4. Agent Guardrails

1. **[PLANNED] = Không implement** — Verify email, Complete profile, Change password, Delete user, Policy management, MFA.
2. **Internal endpoints** — không expose ra public, cần auth riêng hoặc network policy.
3. **Out of scope** — không xác thực credentials, không quản lý session.