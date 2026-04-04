# Task: User Registration

## Trạng thái
- [ ] Task mới — chưa implement

## Definition of Done
- [ ] User tự đăng ký được bằng email + username + password.
- [ ] Sau đăng ký redirect về login page.
- [ ] Validation lỗi hiển thị rõ ràng.
- [ ] Social login (Google) hoạt động từ landing page và login page.

---

## Task 1 — admin-service

### Đọc trước
- `services/admin/CLAUDE.md`
- `docs/conventions/ddd-structure.md`
- `docs/conventions/error-handling.md`
- `services/admin/docs/domains/user.md`
- `services/admin/docs/use-cases/UC-002_register.md`
- `services/admin/docs/use-cases/UC-003_social_register.md`

### Implement
```
UC-002: User tự đăng ký — POST /api/v1/users/register

Lưu ý:
- Không gán role mặc định
- Hash password trước khi lưu
- Dispatch UserCreatedEvent

UC-003: Social register — POST /api/v1/internal/users/social
Verify đã implement chưa — nếu chưa thì implement.

Lưu ý UC-003:
- Internal endpoint — chỉ oauth2 service gọi
- Auto-generate username: email_prefix + timestamp
- usernameChanged = false
- requiresProfileCompletion = true khi tạo mới
```

---

## Task 2 — oauth2-service

### Đọc trước
- `services/oauth2/CLAUDE.md`
- `services/oauth2/docs/flows/001_authentication_flow.md`
- `services/admin/docs/flows/001_social_registration_flow.md`

### Implement
```
Verify social login flow đã xử lý đúng:
- Gọi admin-service UC-003 sau khi xác thực Google thành công
- requiresProfileCompletion flag được trả về trong response
- Lưu flag vào session để web-gateway truyền về Angular

Đọc thêm:
- services/oauth2/docs/use-cases/UC-003_social_register.md
```

---

## Task 3 — Generate API Contract

```
Dựa vào UC file sau, generate OpenAPI 3.0 spec (YAML):
- UC-002: POST /api/v1/users/register

Include: request body, response shape, error codes, HTTP status.
Output: docs/api/admin-user-register.yaml
```

---

## Task 4 — web (Angular) — Logic

### Đọc trước
- `web/CLAUDE.md`
- `docs/api/admin-user-register.yaml`

### Implement
```
Implement logic cho Registration flow.
Chỉ implement TypeScript — HTML là functional placeholder, không cần style.

1. Register page (/register)
   - signals: isLoading
   - Fields: email, username, password, fullName (optional)
   - method register(): POST /api/v1/users/register
     → success: navigate /login với success message
   - method loginWithGoogle(): window.location.href = '/oauth2/authorization/web-gateway'
   - Handle errors: EMAIL_ALREADY_EXISTS, USERNAME_ALREADY_EXISTS

2. Landing page (/) — cập nhật
   - Button "Đăng ký" → navigate /register (bỏ [PLANNED], enable)

3. Login page (/login) — cập nhật
   - Thêm button "Đăng nhập với Google"
   - Thêm link "Chưa có tài khoản? Đăng ký"

4. requiresProfileCompletion handling
   - Sau social login nếu requiresProfileCompletion = true
     → hiển thị nudge banner trong /app/profile
     → không block user
```

### Output sau khi xong
Liệt kê toàn bộ file HTML + CSS vừa tạo hoặc cập nhật cần style — để dùng cho Task 5 (Gemini).

---

## Task 5 — web (Angular) — Styling
> Dùng Gemini — không dùng Claude cho task này.

### Files cần style
Lấy từ output của Task 4 — danh sách HTML + CSS files vừa tạo hoặc cập nhật.

### Gemini prompt
```
You are a senior UI/UX-focused frontend engineer.

Restyle the following files following the design system in @web/docs/layout/dashboard.md.

## CONSTRAINTS
- DO NOT modify any .ts files
- WHEN add new Angular Material imports or new libraries ask me
- DO NOT use inline styles (except dynamic values)
- Tailwind for layout/spacing — Angular Material for components
- Existing files may contain code from previous features — DO NOT modify those parts, reuse existing styles/classes where possible
- If constraint violation is needed, ask me first
- Styling Angular Material (MDC) Components"To reliably style Angular Material components, prioritize overriding their CSS Custom Properties (variables) instead of direct properties like background-color. This is the modern approach for MDC-based components and avoids style conflicts.

## FILES TO RESTYLE
{paste danh sách file từ Task 4 output vào đây — mỗi file gồm .html + .css + context .ts}

## NOTES
- Register page: same card style as login page
- Google login button: white background, Google icon, bordered
- Landing page: hero section với 2 CTA buttons

## OUTPUT
Return complete updated file content for each file.
```