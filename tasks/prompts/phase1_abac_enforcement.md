# Prompt: Phase 0 + Phase 1 — Verify & ABAC Enforcement

**Vai trò**: Bạn là Senior Backend Engineer thực hiện Phase 0 (verify RoleContext migration) và Phase 1 (wire ABAC enforcement) cho `services/admin`. Đây là bước unblock Admin Portal — code phải đúng convention và test được trước khi chuyển sang Angular.

## Tài liệu căn cứ

1. Implementation plan: @docs/context/final_design/07_admin_portal_implementation_plan.md
2. UC-026 Subject Enrichment: @services/admin/docs/use-cases/UC-026_subject_enrichment.md
3. UC-025 PEP Enforcement: @services/admin/docs/use-cases/UC-025_pep_enforcement.md
4. Convention bắt buộc: @docs/conventions/ddd-structure.md
5. @services/admin/SERVICE_MAP.md, @services/admin/CLAUDE.md, @CLAUDE.md

## Phase 0 — Verify (làm trước, fix nếu sai)

Đọc và kiểm tra 4 điểm:

1. `db/migration/V7__migrate_user_roles_to_context.sql` — logic migrate `user_roles` → `user_role_context` với scope=ADMIN, orgId='', sau đó drop `user_roles`
2. `application/user/assign_roles/AssignRoles.java` — không còn reference flat roleIds, dùng `addRoleContext` hoặc `assignRoleToContext`
3. `infrastructure/adapter/abac/AdminSubjectProvider.java` — load roles qua `user.getRoleIdsForScope(Scope.ADMIN, null)`, không phải `user.getRoleIds()`
4. `infrastructure/cross_cutting/config/DataInitializer.java` — seed admin user dùng `UserRoleContextJpaEntity` với scope=ADMIN, orgId=""

## Phase 1 — Implement theo thứ tự

UC-026 trước, UC-025 sau.

1. Fix `AdminSubjectProvider` nếu Phase 0 phát hiện sai
2. Implement `AdminEnvironmentProvider` (returns empty Environment với HashMap rỗng cho global và service)
3. Wire `PipEngine` + `PepEngine` vào `AbacConfig`:
   - `PipEngine(policyProvider, environmentProvider, subjectProvider)` — **không có ResourceAccessConfig**
   - `PepEngine(DecisionStrategy.DEFAULT_DENY)`
   - Public endpoints (`/register`, `/me`, `/me/password`) không cần config đặc biệt — đơn giản là không annotate `@PreEnforce`
4. Annotate controllers `@PreEnforce` + `@ResourceMapping` theo bảng trong UC-025
   - **Bắt buộc**: mọi method có `@PreEnforce` phải có `@ResourceMapping` — thiếu sẽ throw `AuthorizationException` ngay khi startup call đến method đó
   - `/evaluate` (UIElementController) **không annotate** — chicken-and-egg với Angular shell
5. Handle `AuthorizationException` → 403 trong `GlobalExceptionHandler`
6. Seed policy + resources/actions trong `DataInitializer` (xem UC-025 phần seed)
7. Smoke test: admin JWT → 200, no JWT → 401, JWT không có role ADMIN → 403

## Thiết kế quan trọng

- `ResourceAccessConfig` **không dùng** — URL path không thể resolve được action (GET /users → LIST hay READ?). Mọi endpoint enforce phải có `@ResourceMapping` explicit.
- `ignoredPaths` cũng **không dùng** — với `@PreEnforce` annotation-driven, endpoint không có annotation = không enforce, không cần bypass config.
- Resource/action DB entries (seeded qua DataInitializer) phục vụ UIElement binding — enforcement hoạt động bằng string match SpEL, không phụ thuộc DB entries.

**Không implement**: UC-027 visual builder, Angular, Party model.

## Yêu cầu Handoff (bắt buộc)

Sau khi `mvn clean compile -DskipTests` và smoke test xong:

### PHASE 2 CONTEXT BLOCK
- Files đã tạo/sửa (theo layer)
- Policy seed: PolicySet name, Policy name, Rule name + expression đã seed
- Resource/Action names đã seed (để FE bind UIElement sau)
- Pattern decisions nếu khác UC docs
- TODO còn lại nếu có
