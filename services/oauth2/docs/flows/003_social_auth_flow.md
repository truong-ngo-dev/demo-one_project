# Social Authentication Flow

## Tổng quan

Flow xác thực qua social provider (Google) chia làm 3 phase — tương tự form login nhưng Phase 1 khác hoàn toàn:

- **Phase 1 – Social Authentication**: Google xác thực user, oauth2-service tạo hoặc lookup user qua admin-service, enrich principal.
- **Phase 1.5 – Code Issued**: Bridge device info từ HTTP session sang Authorization Record (giống form login).
- **Phase 2 – Token Issued**: Phát hành token kèm `requires_profile_completion`, tạo OAuth Session, ghi Login Activity (giống form login).

Điểm khác biệt cốt lõi so với form login: oauth2-service không kiểm tra credentials — Google là bên xác thực danh tính. User creation/lookup xảy ra trong `SocialLoginOidcUserService.loadUser()`, trước khi Phase 1 kết thúc.

---

## Flow

```
Browser              web-gateway           oauth2-service            Google          admin-service
   │                      │                      │                      │                  │
   │  (1) User click "Đăng nhập" trên landing page hoặc login page      │                  │
   │── GET /oauth2/authorization/web-gateway ───▶│                      │                  │
   │                      │                      │                      │                  │
   │                      │  (2) web-gateway build authorization URL    │                  │
   │                      │      redirect Browser đến oauth2-service    │                  │
   │◀─── redirect /oauth2/authorize?client_id=web-gateway&... ─────────▶│                  │
   │                      │                      │                      │                  │
   │  (3) oauth2-service redirect Browser về login page của chính nó    │                  │
   │◀──────────────────────────── redirect /login ───────────────────────▶(oauth2-service) │
   │                      │                      │                      │                  │
   │  (4) User ở login page, click "Đăng nhập với Google"               │                  │
   │── GET /oauth2/authorization/google ─────────────────────────────────▶(oauth2-service) │
   │                      │                      │                      │                  │
   │                      │  (5) oauth2-service (với tư cách OAuth2 Client) build Google URL
   │◀──────────────────────── redirect accounts.google.com/... ────────▶│                  │
   │                      │                      │                      │                  │
   │  (6) User login & consent trên Google       │                      │                  │
   │──────────────────────────────────────────────────────────────────────────── login ───▶│
   │◀──────────────────────────────────────── redirect (code) ──────────────────────────── │
   │                      │                      │                      │                  │
   │  (7) Browser gửi code về oauth2-service (callback)                 │                  │
   │── GET /login/oauth2/code/google?code=...&state=... ───────────────▶│                  │
   │                      │                      │                      │                  │
   │               [Phase 1: Social Authentication]                     │                  │
   │                      │  (8) oauth2-service exchange code với Google│                  │
   │                      │──────────────────────────── POST /token ───▶│                  │
   │                      │◀───────────────── OIDC token (sub, email) ──│                  │
   │                      │                      │                      │                  │
   │                      │  [SocialLoginOidcUserService.loadUser()]    │                  │
   │                      │── POST /api/v1/internal/users/social ─────────────────────────▶│
   │                      │◀── { userId, requiresProfileCompletion } ──────────────────────│
   │                      │                      │                      │                  │
   │                      │  override sub = userId                      │                  │
   │                      │  store requiresProfileCompletion in session │                  │
   │                      │                      │                      │                  │
   │                      │  [DeviceAwareAuthenticationSuccessHandler]  │                  │
   │                      │  save/update Device, lưu deviceId vào HTTP session             │
   │                      │                      │                      │                  │
   │  (9) oauth2-service redirect Browser về web-gateway với code       │                  │
   │◀────────────── redirect /login/oauth2/code/web-gateway?code=... ────▶(oauth2-service) │
   │                      │                      │                      │                  │
   │── redirect (code) ──▶│                      │                      │                  │
   │                      │                      │                      │                  │
   │                      │─POST /oauth2/token ─▶│                      │                  │
   │                      │                      │                      │                  │
   │               [Phase 1.5: Code Issued]      │                      │                  │
   │                      │  đọc deviceId từ HTTP session               │                  │
   │                      │  copy deviceId vào Authorization Record     │                  │
   │                      │                      │                      │                  │
   │               [Phase 2: Token Issued]       │                      │                  │
   │                      │  thêm claim sub = userId                    │                  │
   │                      │  thêm claim requires_profile_completion     │                  │
   │                      │  thêm claim sid                             │                  │
   │                      │  tạo OAuth Session                          │                  │
   │                      │  ghi Login Activity (SUCCESS)               │                  │
   │                      │                      │                      │                  │
   │                      │◀── token response ───│                      │                  │
   │                      │                      │                      │                  │
   │                      │  parse sid + requires_profile_completion    │                  │
   │                      │  lưu Redis mapping                          │                  │
   │◀─Set-Cookie: SESSION─│                      │                      │                  │
   │                      │                      │                      │                  │
   │  redirect về /app/dashboard                 │                      │                  │
   │  (nếu requiresProfileCompletion = true      │                      │                  │
   │   → hiển thị nudge banner trong profile)    │                      │                  │
```

**Lưu ý về `/oauth2/authorization/google`:**
Endpoint này được Spring Security tự đăng ký trên **oauth2-service** (do `oauth2Login()` được configure ở đó). oauth2-service đóng vai **OAuth2 Client** với Google — không phải Authorization Server. Đây là điểm duy nhất trong toàn hệ thống mà oauth2-service là client.

**Lưu ý về Google login button:**
Chỉ xuất hiện trên **login page của oauth2-service** (Thymeleaf) — không có trên landing page Angular. Angular không thể gọi trực tiếp `/oauth2/authorization/google` vì endpoint đó thuộc oauth2-service, không phải web-gateway.

---

## So sánh với Form Login Flow

| Điểm so sánh                  | Form Login                                | Social Login                                               |
|-------------------------------|-------------------------------------------|------------------------------------------------------------|
| Entry point từ Angular        | `/oauth2/authorization/web-gateway`       | `/oauth2/authorization/web-gateway` (giống nhau)           |
| Login page                    | Thymeleaf của oauth2-service              | Thymeleaf của oauth2-service (giống nhau)                  |
| Xác thực credentials          | oauth2-service kiểm tra qua admin-service | Google xác thực, oauth2-service không kiểm tra credentials |
| User creation/lookup          | Không — user phải tồn tại trước           | `SocialLoginOidcUserService.loadUser()` — tạo nếu chưa có  |
| JWT `sub`                     | userId (từ admin-service Phase 1)         | userId (override từ Google sub trong loadUser)             |
| Login Activity (FAILED)       | Có — ghi khi sai credentials              | Không — Google xử lý auth failure                          |
| `requires_profile_completion` | Không có                                  | Có — true khi user mới, false khi returning user           |
| Phase 1.5, Phase 2            | Giống nhau                                | Giống nhau                                                 |

---

**Lưu ý:**
- `SocialConnection { provider, providerUserId }` được lưu trong admin-service — cơ chế nhận dạng returning social user, không phải JWT `sub`.
- Google `sub` không bao giờ xuất hiện trong JWT phát hành bởi hệ thống — chỉ có internal `userId`.
- `requires_profile_completion = true` khi user mới — UI hiển thị nudge nhắc cập nhật username, không block user.
- Device tracking trong `DeviceAwareAuthenticationSuccessHandler` dùng chung ở form login và social login.

---

## Domain tương tác

| Domain             | Vai trò trong flow                                        | Phase |
|--------------------|-----------------------------------------------------------|-------|
| Device             | Cập nhật hoặc đăng ký device sau khi xác thực thành công  | 1     |
| Activity (SUCCESS) | Ghi sau khi token được phát hành thành công               | 2     |
| Session            | Tạo OAuth Session sau khi token được phát hành            | 2     |

> Login Activity (FAILED) không áp dụng cho social login — Google xử lý failure trước khi request đến oauth2-service.

---

## Tham khảo
- [Authentication Flow (Form Login)](001_authentication_flow.md)
- [UC-003: Social Register — admin-service](../../../admin/docs/use-cases/UC-003_social_register.md)