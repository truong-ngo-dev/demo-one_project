# CLAUDE.md — Web Gateway

## 1. Project Overview

- **Role**: Backend For Frontend (BFF) — OAuth2 Client. Xử lý auth flow, proxy request đến backend services. Không có business logic
- **Stack**: Java 21, Spring Boot 4.x, Spring Cloud Gateway (WebFlux), Redis.
- **Base Package**: `vn.truongngo.apartcom.one.service.webgateway`
- **Session Store**: Redis (`spring-session-data-redis`)

---

## 2. Đọc trước khi làm

| Muốn làm gì                     | Đọc file này trước                                                                  |
|---------------------------------|-------------------------------------------------------------------------------------|
| Implement bất kỳ use case nào   | [Use Case Index](docs/use-cases/UC-000_index.md)                                    |
| Hiểu session model & Redis keys | [Domain](docs/domains/domain.md)                                                    |
| Implement logout redirect       | [ADR-001: Logout Redirect Strategy](docs/decisions/001_logout_redirect_strategy.md) |
| Tra cứu thuật ngữ               | [Glossary](docs/glossary.md) / [Global Glossary](../../docs/glossary.md)            |
| Tích hợp với oauth2 service     | [oauth2 CLAUDE.md](../../services/oauth2/CLAUDE.md)                                 |

---

## 3. Development Commands

```bash
./mvnw clean install     # Build
./mvnw spring-boot:run   # Run
./mvnw test              # Test
```

---

## 4. Agent Guardrails

1. **Không có business logic**: Không tự thêm xử lý nghiệp vụ vào Web Gateway — mọi logic nằm ở backend services.
2. **Angular không bao giờ thấy token**: Token chỉ tồn tại trong Redis — không trả token về Angular dưới bất kỳ hình thức nào.
3. **Token Relay**: Dùng `tokenRelay()` filter — không tự lấy token từ Redis gắn thủ công.
4. **Route config dùng properties**: Dùng `application.properties` cho route config cơ bản. Chỉ dùng Java DSL khi cần custom logic mà properties không handle được.
5. **Out of scope**: Web Gateway không xác thực user, không phân quyền, không lưu business data.