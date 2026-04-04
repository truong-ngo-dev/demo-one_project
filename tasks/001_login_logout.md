# Task: Role CRUD

## Trạng thái
- [ ] Task mới — chưa implement

## Definition of Done
- [ ] Tạo role mới thành công.
- [ ] Xem danh sách roles, search được.
- [ ] Cập nhật description của role.
- [ ] Xóa role không có user dùng.
- [ ] Xóa role đang được dùng → hiển thị lỗi rõ ràng.

---

## Task 1 — admin-service

### Đọc trước
- `services/admin/CLAUDE.md`
- `docs/conventions/ddd-structure.md`
- `docs/conventions/error-handling.md`
- `services/admin/docs/domains/role.md`

### Implement
```
UC-008: Tạo role         — POST   /api/v1/roles
UC-009: Tìm role theo ID — GET    /api/v1/roles/{id}
UC-010: Danh sách roles  — GET    /api/v1/roles
UC-011: Cập nhật role    — PATCH  /api/v1/roles/{id}
UC-012: Xóa role         — DELETE /api/v1/roles/{id}

Đọc thêm:
- services/admin/docs/use-cases/UC-008_create_role.md
- services/admin/docs/use-cases/UC-009_get_role.md
- services/admin/docs/use-cases/UC-010_list_roles.md
- services/admin/docs/use-cases/UC-011_update_role.md
- services/admin/docs/use-cases/UC-012_delete_role.md
```

---

## Task 2 — Generate API Contract

```
Dựa vào các UC file sau, generate OpenAPI 3.0 spec (YAML):
- UC-008: POST   /api/v1/roles
- UC-009: GET    /api/v1/roles/{id}
- UC-010: GET    /api/v1/roles
- UC-011: PATCH  /api/v1/roles/{id}
- UC-012: DELETE /api/v1/roles/{id}

Include: request body, response shape, error codes, HTTP status.
Output: docs/api/admin-role.yaml
```

---

## Task 3 — web (Angular) — Logic

### Đọc trước
- `web/CLAUDE.md`
- `docs/api/admin-role.yaml`

### Implement
```
Implement logic cho Role Management pages.
Chỉ implement TypeScript — HTML là functional placeholder, không cần style.

1. RoleService
   - getRoles(params): GET /api/v1/roles
   - getRoleById(id): GET /api/v1/roles/{id}
   - createRole(data): POST /api/v1/roles
   - updateRole(id, data): PATCH /api/v1/roles/{id}
   - deleteRole(id): DELETE /api/v1/roles/{id}

2. Role list page (/dashboard/roles)
   - signal: roles, isLoading, keyword
   - Load roles khi init, search theo keyword (debounce 300ms)
   - Actions: edit, delete

3. Create role dialog
   - Fields: name (required), description (optional)
   - Handle error: ROLE_ALREADY_EXISTS

4. Edit role dialog
   - Field: description only — name readonly
   - Handle error: ROLE_NOT_FOUND

5. Delete role
   - Confirm dialog trước khi xóa
   - Handle error: ROLE_IN_USE — hiển thị message rõ ràng
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
- If constrain violation is needed ask me

## FILES TO RESTYLE
{paste danh sách file từ Task 3 output vào đây — mỗi file gồm .html + .css + context .ts}

## OUTPUT
Return complete updated file content for each file.
```

---

## Update 1 — Landing page + Role-based routing

✅ Implemented

- Thêm landing page (/)
- authGuard: role = ADMIN → /admin/dashboard, others → /app/dashboard
- adminGuard: role != ADMIN → /app/dashboard
- Routes: / → Landing, /admin/**, /app/**
- Đăng ký button: không gắn chức năng
- Vẫn Output như phía trên sau khi xong task

### Files tạo mới / cần style (cho Task 4):
- `web/src/app/landing/landing.html` + `landing.css`
- `web/src/app/portal/home/home.html`