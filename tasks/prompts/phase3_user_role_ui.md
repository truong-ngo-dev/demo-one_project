# Prompt: Phase 3 — User & Role Management UI

**Vai trò**: Bạn là Senior Full-Stack Engineer verify và hoàn thiện User & Role Management trên Admin Portal. Code cho Phase 3 đã tồn tại (viết trước Phase 1 & 2), nhiệm vụ là xác nhận nó chạy đúng với ABAC enforcement (Phase 1) và Angular Shell (Phase 2), fix bất kỳ mismatch nào tìm thấy, sau đó smoke test end-to-end.

## Tài liệu căn cứ

1. Handoff Phase 2: `@tasks/log/phase2_angular_shell_handoff.md`
2. Implementation plan: `@docs/context/final_design/07_admin_portal_implementation_plan.md`
3. `@services/admin/CLAUDE.md`, `@services/admin/SERVICE_MAP.md`
4. `@web/CLAUDE.md`

---

## Trạng thái hiện tại (đọc trước khi làm)

Các thành phần sau ĐÃ có — không tạo lại từ đầu:

### Frontend (web)
| File                                                 | Trạng thái                                                                                            |
|------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `web/src/app/core/services/user.service.ts`          | Có — UserService với getUsers, getUserById, createUser, lockUser, unlockUser, assignRoles, removeRole |
| `web/src/app/core/services/role.service.ts`          | Có — RoleService với getRoles, getRoleById, createRole, updateRole, deleteRole                        |
| `web/src/app/dashboard/users/users.ts` + `.html`     | Có — danh sách user + search/filter + lock/unlock                                                     |
| `web/src/app/dashboard/users/user-detail/`           | Có — user detail với 2 tab: Profile và Security & Devices                                             |
| `web/src/app/dashboard/users/create-user-dialog/`    | Có                                                                                                    |
| `web/src/app/dashboard/users/assign-roles-dialog/`   | Có                                                                                                    |
| `web/src/app/dashboard/users/lock-confirm-dialog/`   | Có                                                                                                    |
| `web/src/app/dashboard/roles/roles.ts` + `.html`     | Có — danh sách role + search + create/edit/delete                                                     |
| `web/src/app/dashboard/roles/create-role-dialog/`    | Có                                                                                                    |
| `web/src/app/dashboard/roles/edit-role-dialog/`      | Có                                                                                                    |
| `web/src/app/dashboard/roles/confirm-delete-dialog/` | Có                                                                                                    |

### Backend (admin-service)
| File | Trạng thái |
|------|------------|
| `presentation/user/UserController.java` | Có — 7 endpoints (CREATE, READ, LIST, LOCK, UNLOCK, ASSIGN_ROLE, REMOVE_ROLE) với `@PreEnforce + @ResourceMapping` |
| `presentation/role/RoleController.java` | Có — 5 endpoints (CREATE, READ, LIST, UPDATE, DELETE) với `@PreEnforce + @ResourceMapping` |

### Routing
- `/admin/users` và `/admin/users/:id` → `abacGuard('route:users')` ✅
- `/admin/roles` → `abacGuard('route:roles')` ✅

---

## Bước 1 — Verify response format alignment

Đọc các file sau và kiểm tra shape:

1. **Backend `PagedApiResponse<T>`** (`presentation/base/PagedApiResponse.java`) → trả `{ data: [...], meta: { page, size, total } }`
2. **Frontend `UserPage`** (`user.service.ts`) → expect `{ data: UserSummary[], meta: { page, size, total } }`
3. **Frontend `RolePage`** (`role.service.ts`) → expect `{ data: RoleSummary[], meta: { page, size, total } }`

**Nếu có mismatch** — fix ngay trong service. **Nếu đúng** — ghi note "format OK" và tiếp tục.

Tương tự kiểm tra:
- `getUserById()` expect `{ data: UserDetail }` — backend `ApiResponse<FindUserById.UserDetail>` trả gì
- `createUser()` expect `{ data: { id, username } }` — backend `ApiResponse<AdminCreateUser.Result>` trả gì

---

## Bước 2 — Verify ABAC button guard (btn:user:lock)

`btn:user:lock` UIElement đã được seed ở Phase 2 → `user:LOCK`. Kiểm tra:

1. `abac.service.ts` — `ADMIN_ROUTE_ELEMENT_IDS` có chứa `'btn:user:lock'` chưa
2. `users.html` — nút lock trong table row có `@if (abacService.isPermitted('btn:user:lock'))` chưa
3. `user-detail.html` — nút Lock User / Unlock User trong page header có `@if (abacService.isPermitted('btn:user:lock'))` chưa

**Nếu thiếu** — thêm vào. **Nếu đủ** — ghi note "guard OK" và tiếp tục.

---

## Bước 3 — Fix users list (thiếu paginator)

`users.ts` hiện chỉ gọi `getUsers()` không có pagination — tất cả user trả về trong 1 request. Kiểm tra xem backend có default page size không. Nếu API trả thiếu data do pagination:

**Option A (ưu tiên)**: Thêm `size=200` vào request để lấy đủ data trong giai đoạn này — tránh implement paginator phức tạp.

```typescript
this.userService.getUsers({ keyword: ..., status, size: 200 }).subscribe(...)
```

**Option B**: Implement `mat-paginator` trong `users.html` nếu số lượng user lớn (hỏi trước khi implement).

---

## Bước 4 — Verify create user dialog: roles dropdown

`create-user-dialog` cần gọi `RoleService.getRoles()` để populate multi-select roleIds. Đọc `create-user-dialog.ts` và kiểm tra:

1. Có inject `RoleService` chưa
2. Có gọi `getRoles()` trong `ngOnInit` chưa
3. Multi-select hiển thị đúng `role.name` chưa

Fix nếu thiếu.

---

## Bước 5 — Verify assign roles dialog: chỉ hiện roles chưa gán

`assign-roles-dialog` nhận vào `assignedRoles` (roles user đã có). Cần lọc ra các role chưa gán để tránh duplicate. Đọc `assign-roles-dialog.ts` và kiểm tra:

1. Có lọc `allRoles.filter(r => !assignedRoles.some(ar => ar.id === r.id))` chưa

Fix nếu thiếu.

---

## Bước 6 — Verify error handling

Kiểm tra các error code quan trọng đã được handle:

| Endpoint | Error code | Hiển thị |
|----------|------------|----------|
| POST /users | `EMAIL_ALREADY_EXISTS` | "Email already in use" |
| POST /users | `USERNAME_ALREADY_EXISTS` | "Username already in use" |
| POST /roles | `ROLE_ALREADY_EXISTS` | "Role name already exists" |
| DELETE /roles/:id | `ROLE_IN_USE` | "Cannot delete: role is in use" |
| POST /users/:id/lock or unlock | `INVALID_STATUS` | "Cannot perform: invalid status" |

Đọc từng dialog `.ts` — nếu thiếu error handler, thêm vào `error` callback của subscribe.

---

## Bước 7 — mvn compile + ng build verify

```bash
cd services/admin && mvn compile -DskipTests   # phải BUILD SUCCESS
cd web && ng build --configuration development  # phải không có error
```

Fix bất kỳ compile error nào tìm thấy.

---

## Quy tắc

- Không tạo component/service mới trừ khi verify thấy thiếu hẳn
- Fix tối thiểu — chỉ sửa đúng chỗ bị mismatch
- Không refactor code không liên quan
- `btn:user:lock` là UIElement duy nhất cần guard ở Phase 3 — không thêm guard cho các nút roles (create/edit/delete roles) chưa có UIElement tương ứng

---

## Yêu cầu Handoff (bắt buộc)

Sau khi xong, viết `tasks/log/phase3_user_role_handoff.md` gồm:

- Danh sách fix đã thực hiện (format mismatch, missing guard, v.v.)
- Danh sách verify đã pass (không cần sửa)
- Build status (admin + web)
- TODO còn lại cho Phase 4
