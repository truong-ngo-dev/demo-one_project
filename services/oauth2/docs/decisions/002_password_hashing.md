# ADR-002: Password Hashing Strategy

- **Trạng thái**: Accepted
- **Ngày**: 2025-06
- **Người quyết định**: Backend Team

---

## Bối cảnh

OAuth2 service xác thực user bằng username/password. Password cần được hash trước khi lưu (hoặc so sánh) để đảm bảo bảo mật. Tuy nhiên, trong môi trường dev, việc hash làm chậm quá trình debug và test — đặc biệt khi cần seed dữ liệu thủ công vào DB.

Yêu cầu:
- **Dev**: Dễ seed data, debug nhanh, không cần quan tâm đến bảo mật thực sự.
- **Prod**: Bảo mật tối đa, chống brute-force, chống rainbow table attack.

---

## Quyết định

| Môi trường | Encoder                 | Lý do                                                                |
|------------|-------------------------|----------------------------------------------------------------------|
| `dev`      | `NoOpPasswordEncoder`   | Password lưu plaintext — seed data và debug không cần bước hash.     |
| `prod`     | `BCryptPasswordEncoder` | Adaptive hashing, tích hợp salt, chống brute-force bằng cost factor. |

Cấu hình được tách biệt hoàn toàn tại:
- `application-dev.properties` → `NoOp`
- `application-prod.properties` → `BCrypt`

**Quy tắc bắt buộc**: Không được hardcode `PasswordEncoder` bean trong code. Phải đọc từ config để đảm bảo tách biệt môi trường.

---

## Hệ quả

**Tích cực:**
- Dev workflow nhanh — không cần pre-hash password khi seed DB.
- Prod an toàn — BCrypt với cost factor mặc định (10) đủ chậm để chống brute-force.
- Tách biệt config theo môi trường, không có risk nhầm lẫn giữa dev và prod.

**Đánh đổi:**
- Nếu deploy sai profile (chạy prod code với dev config), password sẽ không được hash — cần đảm bảo CI/CD luôn set đúng Spring profile.
- BCrypt không hỗ trợ verify mà không có plaintext — không thể "decrypt" password đã hash, chỉ có thể compare.

---

## Ghi chú cho Agent
Khi chỉnh sửa bất kỳ security config nào liên quan đến password, **bắt buộc kiểm tra và cập nhật cả hai file** `application-dev.properties` và `application-prod.properties`.

---

## Tham khảo
- [Spring Security – Password Storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)
- [BCrypt – Cost Factor Recommendations](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)