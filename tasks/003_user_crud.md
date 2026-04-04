# Task: User CRUD

## Trạng thái
- [ ] Task mới — chưa implement

## Definition of Done
- [ ] Admin tạo user mới với role.
- [ ] Xem danh sách users, search/filter được.
- [ ] Xem chi tiết user.
- [ ] Lock/unlock user.
- [ ] Gán/gỡ role cho user.

---

## Task 1 — admin-service

### Đọc trước
- `services/admin/CLAUDE.md`
- `docs/conventions/ddd-structure.md`
- `docs/conventions/error-handling.md`
- `services/admin/docs/domains/user.md`
- `services/admin/docs/domains/role.md`

### Implement
```
UC-001: Admin tạo user    — POST   /api/v1/users
UC-004: Tìm user theo ID  — GET    /api/v1/users/{id}
UC-006: Search user       — GET    /api/v1/users
UC-007: Lock/Unlock user  — POST   /api/v1/users/{id}/lock
                            POST   /api/v1/users/{id}/unlock
UC-013: Gán role cho user — POST   /api/v1/users/{id}/roles
UC-014: Gỡ role khỏi user — DELETE /api/v1/users/{id}/roles/{roleId}

Đọc thêm:
- services/admin/docs/use-cases/UC-001_admin_create_user.md
- services/admin/docs/use-cases/UC-004_get_user.md
- services/admin/docs/use-cases/UC-006_search_user.md
- services/admin/docs/use-cases/UC-007_lock_unlock_user.md
- services/admin/docs/use-cases/UC-013_assign_roles.md
- services/admin/docs/use-cases/UC-014_remove_role.md
```

---

## Task 2 — Generate API Contract

```
Dựa vào các UC file sau, generate OpenAPI 3.0 spec (YAML):
- UC-001: POST   /api/v1/users
- UC-004: GET    /api/v1/users/{id}
- UC-006: GET    /api/v1/users
- UC-007: POST   /api/v1/users/{id}/lock
          POST   /api/v1/users/{id}/unlock
- UC-013: POST   /api/v1/users/{id}/roles
- UC-014: DELETE /api/v1/users/{id}/roles/{roleId}

Include: request body, response shape, error codes, HTTP status.
Output: docs/api/admin-user.yaml
```

---

## Task 3 — web (Angular) — Logic

### Đọc trước
- `web/CLAUDE.md`
- `docs/api/admin-user.yaml`
- `docs/api/admin-role.yaml`

### Implement
```
Implement logic cho User Management pages.
Chỉ implement TypeScript — HTML là functional placeholder, không cần style.

1. UserService
   - getUsers(params): GET /api/v1/users
   - getUserById(id): GET /api/v1/users/{id}
   - createUser(data): POST /api/v1/users
   - lockUser(id): POST /api/v1/users/{id}/lock
   - unlockUser(id): POST /api/v1/users/{id}/unlock
   - assignRoles(userId, roleIds): POST /api/v1/users/{id}/roles
   - removeRole(userId, roleId): DELETE /api/v1/users/{id}/roles/{roleId}

2. User list page (/dashboard/users)
   - signals: users, isLoading, keyword, statusFilter
   - Load users khi init, search theo keyword (debounce 300ms), filter theo status
   - Actions: view, lock/unlock

3. User detail page (/dashboard/users/{id})
   - signals: user, isLoading
   - Hiển thị: email, username, fullName, status, roles, socialConnections
   - Actions: lock/unlock, assign/remove role

4. Create user dialog
   - Fields: email (required), username (required), fullName (optional), roleIds (multi-select)
   - Fetch roles từ GET /api/v1/roles để populate multi-select
   - Handle errors: EMAIL_ALREADY_EXISTS, USERNAME_ALREADY_EXISTS, ROLE_NOT_FOUND

5. Assign role dialog
   - Multi-select roles chưa được gán
   - Handle error: ROLE_NOT_FOUND

6. Lock/unlock confirmation dialog
   - Confirm trước khi thực hiện
   - Handle error: INVALID_STATUS
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
- DO NOT add new Angular Material imports
- DO NOT add new libraries
- DO NOT use inline styles (except dynamic values)
- Tailwind for layout/spacing — Angular Material for components
- Existing files may contain code from previous features — DO NOT modify those parts, reuse existing styles/classes where possible

## FILES TO RESTYLE
{paste danh sách file từ Task 3 output vào đây — mỗi file gồm .html + .css + context .ts}

## OUTPUT
Return complete updated file content for each file.
```