# CLAUDE.md – OAuth2 Service

## 1. Project Overview

- **Role**: Identity Provider (IdP) & Authorization Server. Source of Truth cho Sessions, Devices, và Activities. Không lưu User Profile — fetch từ Admin Service khi cần.
- **Stack**: Java 21, Spring Boot 4.x, Spring Authorization Server, MySQL.
- **Base Package**: `vn.truongngo.apartcom.one.service.oauth2`
- **Architecture**: DDD (Vertical Slice), Database per service (Isolated MySQL).

---

## 2. Đọc trước khi làm

| Muốn làm gì                    | Đọc file này trước                                                    |
|--------------------------------|-----------------------------------------------------------------------|
| **Hiểu cấu trúc code/package** | [**SERVICE_MAP.md**](SERVICE_MAP.md)                                  |
| **Tìm hiểu Domain Model**      | [Device](docs/domains/device.md), [Session](docs/domains/session.md), [Activity](docs/domains/activity.md), [User](docs/domains/user.md) |
| Implement bất kỳ use case nào  | [Use Case Index](docs/use-cases/UC-000_index.md)                      |
| Hiểu flow xác thực             | [Authentication Flow](docs/flows/001_authentication_flow.md)          |
| Hiểu flow logout               | [Logout Flow](docs/flows/002_logout_flow.md)                          |
| Tra cứu thuật ngữ              | [Glossary](docs/glossary.md)                                          |
| Tích hợp với Web Gateway       | [Web Gateway Mapping](../../services/web-gateway/CLAUDE.md)           |

---

## 3. Project Mapping (DDD Layers)

Xem chi tiết tại [SERVICE_MAP.md](SERVICE_MAP.md). Tóm tắt:
- **Domain**: [`device`](docs/domains/device.md), [`session`](docs/domains/session.md), [`activity`](docs/domains/activity.md), [`user`](docs/domains/user.md) (Projection).
- **Application**: Commands/Queries phân theo slice.
- **Infrastructure**:
    - `persistence/`: JPA entities & adapters.
    - `security/`: OAuth2/Spring Authorization Server config (đây là phần phức tạp nhất).
    - `api/http/internal/admin/`: Outbound client gọi sang Admin Service.
- **Presentation**: REST Controllers và Login Controller (MVC).

---

## 4. Development Commands

```bash
./mvnw clean install     # Build
./mvnw spring-boot:run   # Run
./mvnw test              # Test
```

---

## 4. Agent Guardrails

1. **[PLANNED] = Không implement**: Social Login, Key Rotation, MFA, Cache, Session Expiry là các feature chưa được thiết kế. Không tự ý viết code cho các phần này trừ khi được yêu cầu rõ ràng.
2. **Cần thông tin User?**: Nếu task yêu cầu thông tin ngoài `userId`, hỏi về API endpoint của Admin Service trước khi làm.
3. **Sửa security config?**: Luôn kiểm tra và cập nhật cả `application-dev.properties` lẫn `application-prod.properties`.
4. **Out of scope**: Service này không quản lý role/permission, không lưu user profile, không làm authorization policy — những thứ đó thuộc Admin Service hoặc Resource Server.