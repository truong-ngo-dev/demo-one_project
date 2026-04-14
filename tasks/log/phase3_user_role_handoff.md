# Phase 3 User & Role UI — Handoff Context

*Completed: 2026-04-13*

---

## Verify pass (không cần sửa)

| Item | Kết quả |
|------|---------|
| `PagedApiResponse<T>` → `{ data, meta: { page, size, total } }` khớp `UserPage` / `RolePage` | ✅ |
| `ApiResponse<T>` → `{ data: T }` khớp `getUserById()` và `createUser()` | ✅ |
| Backend `UserSummary` / `RoleSummary` fields khớp frontend interfaces | ✅ |
| `FindUserById.UserDetail` fields (kể cả `SocialConnectionView.connectedAt: Long`) khớp frontend | ✅ |
| `Instant createdAt` → Spring Boot auto-config `write-dates-as-timestamps=false` → ISO-8601 string → `DatePipe` OK | ✅ |
| `ADMIN_ROUTE_ELEMENT_IDS` chứa `btn:user:lock` | ✅ |
| `users.html` lock button wrapped `@if (abacService.isPermitted('btn:user:lock'))` | ✅ |
| `user-detail.html` Lock/Unlock button wrapped `@if (abacService.isPermitted('btn:user:lock'))` | ✅ |
| `create-user-dialog.ts` inject `RoleService`, gọi `getRoles({ size: 100 })` trong `ngOnInit` | ✅ |
| `assign-roles-dialog.ts` lọc roles chưa gán: `filter(r => !assignedIds.has(r.id))` | ✅ |
| Error handling: `EMAIL_ALREADY_EXISTS`, `USERNAME_ALREADY_EXISTS`, `ROLE_NOT_FOUND`, `ROLE_ALREADY_EXISTS`, `ROLE_IN_USE`, `INVALID_STATUS` | ✅ |

---

## Fix đã thực hiện

| File | Fix |
|------|-----|
| `web/src/app/dashboard/users/users.ts` | Thêm `size: 100` vào `getUsers()` call — tránh default page size 20 cắt mất user |

---

## Build status

```
services/admin: mvn compile -DskipTests → BUILD SUCCESS
web: ng build --configuration development → BUILD SUCCESS (Application bundle generation complete)
```

---

## Pattern decisions

**Không implement paginator**: Users list dùng `size: 100` (max backend cho phép). Paginator sẽ implement khi user count đủ lớn để cần thiết.

**Không thêm UIElement cho roles CRUD buttons**: `btn:user:lock` là UIElement button duy nhất được seed ở Phase 2. Role create/edit/delete buttons không có UIElement tương ứng — chỉ được bảo vệ bởi backend ABAC (`@PreEnforce`).

---

## TODO cho Phase 4 (ABAC Management UI)

Phase 4 (Resource + Action CRUD, PolicySet/Policy/Rule, UIElement registry, Simulator, Audit log) đã implement từ trước — tương tự Phase 3, cần verify + smoke test khi chạy hệ thống.
