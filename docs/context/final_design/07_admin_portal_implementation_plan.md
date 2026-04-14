# Admin Portal — Implementation Plan

*2026-04-13*

## Mục tiêu
Dựng Admin Portal (ADMIN scope) trước, dùng nó để config ABAC cho các portal sau.
Party model defer hoàn toàn cho đến khi bắt đầu Operator Portal.

## Sequence

```
Phase 0 — Verify & Stabilize   (1–2 ngày)
Phase 1 — ABAC Enforcement      (3–4 ngày)   ← unblock self-protection
Phase 2 — Angular Shell         (2–3 ngày)
Phase 3 — User & Role UI        (4–5 ngày)
Phase 4 — ABAC Management UI   (5–7 ngày)
Phase 5 — Session & Activity    (2–3 ngày)
```

---

## Phase 0 — Verify & Stabilize

| Task | Nội dung                                                                         |
|------|----------------------------------------------------------------------------------|
| 0.1  | Chạy V7 migration, verify `user_roles` đã drop, `user_role_context` có data đúng |
| 0.2  | Smoke test UC-013 AssignRoles, UC-014 RemoveRole — behavior đúng với RoleContext |
| 0.3  | Smoke test UC-004 FindUserById — trả đúng roles từ ADMIN context                 |
| 0.4  | Verify `AdminSubjectProvider` dùng `getRoleIdsForScope(ADMIN, null)`             |

---

## Phase 1 — ABAC Enforcement

UC docs đã đủ chi tiết, implement trực tiếp từ:
- `services/admin/docs/use-cases/UC-026_subject_enrichment.md`
- `services/admin/docs/use-cases/UC-025_pep_enforcement.md`

**Thứ tự**: UC-026 trước (Subject phải đúng trước khi enforce).

| Task | Nội dung                                                                                               |
|------|--------------------------------------------------------------------------------------------------------|
| 1.1  | Fix UC-026: `AdminSubjectProvider` — thay `user.getRoleIds()` → `user.getRoleIdsForScope(ADMIN, null)` |
| 1.2  | Implement `AdminEnvironmentProvider` (trả empty Environment)                                           |
| 1.3  | Wire `PipEngine` + `PepEngine` vào `AbacConfig`                                                        |
| 1.4  | Annotate controllers với `@PreEnforce` + `@ResourceMapping`                                            |
| 1.5  | Seed policy: PolicySet `admin-root` (scope=ADMIN, isRoot=true) + Policy + Rules cơ bản                 |
| 1.6  | Seed UIElements cho từng nhóm màn hình                                                                 |
| 1.7  | Test end-to-end: admin login → PERMIT; non-admin → 403                                                 |

**Deliverable**: Backend tự bảo vệ bằng ABAC.

---

## Phase 2 — Angular Shell

Task file: `tasks/013_admin_portal_shell.md` (viết trước khi implement)

| Task | Nội dung                                                            |
|------|---------------------------------------------------------------------|
| 2.1  | Angular app setup — routing, layout shell (sidebar + header)        |
| 2.2  | Auth flow: OAuth2 redirect → callback → session cookie              |
| 2.3  | Batch evaluate UIElements khi load layout — visibility map cho menu |
| 2.4  | UC-028 Route Guard                                                  |
| 2.5  | Global 403 handler                                                  |

---

## Phase 3 — User & Role Management

| Task | Nội dung                                      |
|------|-----------------------------------------------|
| 3.1  | Role list/create/edit/delete                  |
| 3.2  | User list với search/filter                   |
| 3.3  | User detail + role assignment (ADMIN context) |
| 3.4  | Lock/Unlock user                              |

---

## Phase 4 — ABAC Management

Expression-only (raw SpEL), visual builder là Phase sau.

| Task | Nội dung                                |
|------|-----------------------------------------|
| 4.1  | Resource + Action CRUD                  |
| 4.2  | PolicySet/Policy/Rule CRUD + reorder    |
| 4.3  | UIElement registry + coverage indicator |
| 4.4  | Policy Simulator (UC-024/033)           |
| 4.5  | Navigation Simulate (UC-031/034)        |
| 4.6  | Audit log viewer (UC-035)               |

---

## Phase 5 — Session & Activity

| Task | Nội dung                                       |
|------|------------------------------------------------|
| 5.1  | Session list per user (query `oauth_sessions`) |
| 5.2  | Admin force terminate session                  |
| 5.3  | Login activity log                             |

---

## Defer hoàn toàn

- UC-027 Expression visual builder
- Party model
- Session expiry cleanup job (`docs/context/final_design/session_expiry_cleanup_analysis.md`)
- UC-029 Policy Decision Audit Log
