# Authentication Flow

## Tổng quan

Flow xác thực chia làm 3 phase:

- **Phase 1 – Authentication**: Xác minh danh tính, cập nhật Device.
- **Phase 1.5 – Code Issued**: Bridge device info từ HTTP session sang Authorization Record.
- **Phase 2 – Token Issued**: Phát hành token, tạo OAuth Session, ghi Login Activity.

Phase 1.5 tồn tại vì Phase 1 (Authorization Endpoint) và Phase 2 (Token Endpoint) chạy trên hai request riêng biệt — cần một bước trung gian để truyền context giữa hai phase.

---

## Flow

```
Browser                oauth2-service               admin-service        web-gateway
   │                        │                             │                    │
   │──── POST /login ──────▶│                             │                    │
   │                  [Phase 1: Authentication]           │                    │
   │                  xác thực credentials                │                    │
   │                        │──── GET /internal/users/identity ───────────────▶│
   │                        │◀── userId, account status ──│                    │
   │                        │                             │                    │
   │                  [THÀNH CÔNG]                        │                    │
   │                  tạo hoặc cập nhật Device            │                    │
   │                  lưu deviceId vào HTTP session       │                    │
   │◀─── redirect + code ───│                             │                    │
   │                        │                             │                    │
   │──── redirect (code) ─────────────────────────────────────────────────────▶│
   │                        │                             │                    │
   │                        │◀──── POST /oauth2/token ─────────────────────────│
   │                  [Phase 1.5: Code Issued]            │                    │
   │                  đọc deviceId + idpSessionId từ HTTP session              │
   │                  copy chúng vào Authorization Record attributes           │
   │                        │                             │                    │
   │                  [Phase 2: Token Issued]             │                    │
   │                  thêm claim `sid` vào JWT            │                    │
   │                  tạo OAuth Session                   │                    │
   │                  ghi Login Activity (SUCCESS)        │                    │
   │                        │                             │                    │
   │                        │──── token response ─────────────────────────────▶│
   │                        │                             │                    │
   │                  [THẤT BẠI]                          │                    │
   │                  user tồn tại trên hệ thống?         │                    │
   │                  ├─ CÓ → ghi Login Activity (FAILED) │                    │
   │                  └─ KHÔNG → bỏ qua, không ghi        │                    │
   │◀─── 401 Unauthorized ──│                             │                    │
```

**Lưu ý:**
- Phase 1.5 là bước trung gian — device info được bridge từ HTTP session sang Authorization Record attributes vì hai phase chạy trên hai request khác nhau.
- Chỉ nhận `userId` từ Admin Service — không lưu thêm bất kỳ thông tin profile nào.
- Nếu Admin Service không phản hồi, authentication fail ngay tại Phase 1.
- Login Activity (FAILED) chỉ được ghi khi user thực sự tồn tại trên hệ thống — nếu username không tồn tại (Admin Service trả 404), không ghi activity. Tránh tạo record rác cho các request brute-force với username ngẫu nhiên.
- OAuth Session chỉ được tạo sau khi Authorization Record đã persist thành công.
- `sid` là cầu nối giữa token và OAuth Session — không thay đổi mapping này.

---

## Domain tương tác

| Domain             | Vai trò trong flow                                       | Phase |
|--------------------|----------------------------------------------------------|-------|
| Device             | Cập nhật hoặc đăng ký device sau khi xác thực thành công | 1     |
| Activity (FAILED)  | Ghi khi xác thực thất bại, user tồn tại trên hệ thống    | 1     |
| Activity (SUCCESS) | Ghi sau khi token được phát hành thành công              | 2     |
| Session            | Tạo OAuth Session sau khi token được phát hành           | 2     |

---

## Tham khảo
- [Glossary](../glossary.md)
- [UC-001: Login](../use-cases/UC-001_login.md)
- [ADR-001: Token Format](../decisions/001_token_format.md)