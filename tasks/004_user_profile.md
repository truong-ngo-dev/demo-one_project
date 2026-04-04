# Task: User Profile & Account

## Trạng thái
- [ ] Task mới — chưa implement

## Definition of Done
- [ ] User xem được profile của mình.
- [ ] User cập nhật được thông tin cá nhân (username lần đầu, fullName, phoneNumber).
- [ ] User đổi được password.
- [ ] Avatar dropdown trong header hiển thị đúng menu.

---

## Task 1 — admin-service

### Đọc trước
- `services/admin/CLAUDE.md`
- `docs/conventions/ddd-structure.md`
- `docs/conventions/error-handling.md`
- `services/admin/docs/domains/user.md`

### Implement
```
UC-016: Update profile — PATCH /api/v1/users/me
UC-017: Change password — POST /api/v1/users/me/password

Đọc thêm:
- services/admin/docs/use-cases/UC-016_update_profile.md
- services/admin/docs/use-cases/UC-017_change_password.md

Lưu ý UC-016:
- Lấy userId từ Access Token (sub claim) — không nhận userId từ request path
- username chỉ cho đổi nếu usernameChanged = false

Lưu ý UC-017:
- Social-only user (password null): chỉ cần newPassword, không cần currentPassword
- User có password: bắt buộc currentPassword
- New password trùng current: trả 200 OK { changed: false, message: ... }
```

---

## Task 2 — Generate API Contract

```
Dựa vào các UC file sau, generate OpenAPI 3.0 spec (YAML):
- UC-016: PATCH /api/v1/users/me
- UC-017: POST  /api/v1/users/me/password

Include: request body, response shape, error codes, HTTP status.
Output: docs/api/admin-user-self.yaml
```

---

## Task 3 — web (Angular) — Logic

### Đọc trước
- `web/CLAUDE.md`
- `docs/api/admin-user-self.yaml`

### Implement
```
Implement logic cho User Profile pages.
Chỉ implement TypeScript — HTML là functional placeholder, không cần style.

1. UserSelfService (tách riêng khỏi UserService dùng cho admin)
   - getMe(): GET /api/v1/users/me  (nếu chưa có thì dùng thông tin từ session)
   - updateProfile(data): PATCH /api/v1/users/me
   - changePassword(data): POST /api/v1/users/me/password

2. App Header — Avatar dropdown (/app layout)
   - Hiển thị: avatar/initials + username
   - Menu items:
     - "Thông tin cá nhân" → navigate /app/profile
     - "Đổi mật khẩu" → navigate /app/profile/password
     - "Đăng xuất" → AuthService.logout()

3. Profile page (/app/profile)
   - signals: user, isLoading, isSaving
   - Hiển thị: email (readonly), username, fullName, phoneNumber
   - username: readonly nếu usernameChanged = true, editable nếu false
   - Submit: PATCH /api/v1/users/me
   - Handle errors: USERNAME_ALREADY_EXISTS, USERNAME_ALREADY_CHANGED, PHONE_ALREADY_EXISTS

4. Change password page (/app/profile/password)
   - signals: isSaving, hasPassword (để ẩn/hiện currentPassword field)
   - Fields:
     - currentPassword (ẩn nếu social-only user)
     - newPassword
   - Handle errors: CURRENT_PASSWORD_REQUIRED, INVALID_PASSWORD
   - Handle same password: hiển thị info message (không phải error)

5. Shared component: UserAvatarComponent
   - Dùng chung cho cả /admin và /app header
   - Input: username, fullName
   - Hiển thị initials nếu không có avatar
```

### Output sau khi xong
Liệt kê toàn bộ file HTML + CSS vừa tạo cần style — để dùng cho Task 4 (Gemini).

---

## Task 4 — web (Angular) — Styling
> Dùng Gemini — không dùng Claude cho task này.

### Files cần style
Lấy từ output của Task 3 — danh sách HTML + CSS files vừa tạo.

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

## FILES TO RESTYLE
{paste danh sách file từ Task 3 output vào đây — mỗi file gồm .html + .css + context .ts}

## NOTES
- Avatar dropdown dùng MatMenu
- Profile form dùng Angular Material form fields
- Change password form: ẩn/hiện currentPassword field dựa trên hasPassword signal

## OUTPUT
Return complete updated file content for each file.
``