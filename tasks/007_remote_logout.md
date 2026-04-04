# Task: Remote Logout (UC-008)

## Trạng thái
- [x] Task 1 — oauth2-service: Remote Revoke Logic
- [x] Task 2 — oauth2-service: Web Gateway Integration (Notify)
- [x] Task 3 — OpenAPI contract: Update `docs/api/oauth2-sessions-history.yaml`
- [x] Task 4 — web (Angular): Action Logic

## Definition of Done
- [x] User có thể đăng xuất từ xa một thiết bị cụ thể từ danh sách thiết bị.
- [x] Không thể tự đăng xuất thiết bị hiện tại qua endpoint này (trả về lỗi hoặc yêu cầu dùng Logout chuẩn).
- [x] Web Gateway nhận được notify và xóa session trong Redis ngay lập tức (User bị đá văng khỏi app ở thiết bị kia).
- [x] Trạng thái session trong DB chuyển sang `REVOKED`.
- [x] Giao diện Angular cập nhật lại danh sách ngay sau khi thực hiện.

---

## Task 1 — oauth2-service: Remote Revoke Logic

### Đọc trước
- `services/oauth2/docs/domains/session.md`
- `services/oauth2/docs/use-cases/UC-008_remote_logout.md`

### Implement
```
Endpoint: DELETE /api/v1/sessions/me/{sessionId}

1. Application Layer:
   - Command: `RemoteRevokeSession(sessionId, currentUserId, currentSid)`
   - Handler:
     · Tìm Oauth2Session theo sessionId.
     · Invariants: session.userId == currentUserId, status == ACTIVE.
     · Guard: sessionId != currentSid.
     · Gọi OAuth2AuthorizationRevocationPort.deleteByAuthorizationId(session.springAuthorizationId()).
     · session.revoke() (chuyển status, sinh SessionRevokedEvent).
     · sessionRepository.save(session).
     · eventDispatcher.dispatchAll(session.pullDomainEvents()).

2. Infrastructure Layer (Persistence):
   - Port Extension: `OAuth2AuthorizationRevocationPort` thêm `deleteByAuthorizationId(String id)`.
   - Persistence Adapter: Implement trong `JdbcOAuth2AuthorizationServiceExtends` (xóa bằng `id` của authorization record).

3. Presentation Layer:
   - Controller: `SessionController` expose endpoint `DELETE /api/v1/sessions/me/{sessionId}`.
   - Extract `userId` (sub) và `sid` (sessionId) từ JWT @AuthenticationPrincipal.
```

---

## Task 2 — oauth2-service: Web Gateway Integration

### Đọc trước
- `services/web-gateway/docs/use-cases/UC-003_revoke_session.md`

### Implement
```
Khi session bị revoke, cần notify cho Web Gateway để invalidate Redis session.

1. Event Handler: `SessionRevokedEventHandler`
   - Listen `SessionRevokedEvent`.
   - Lấy `sid` (chính là aggregateId của event - sessionId).
   - Gọi `WebGatewayClient.notifyRevocation(sid)`.

2. Outbound Client: `WebGatewayClient`
   - Dùng `RestClient` gọi `POST /webgw/internal/sessions/revoke`.
   - Body: `{ "sid": "{sessionId}" }`.
   - Base URL: Config trong `application.properties` (ví dụ: `app.gateway-service.base-url`).
```

---

## Task 3 — OpenAPI Contract

```
Cập nhật docs/api/oauth2-sessions-history.yaml:
- Thêm path: DELETE /api/v1/sessions/me/{sessionId}
- Parameters: sessionId (path, required)
- Response: 204 No Content
- Error Codes:
  · 02001 (SESSION_NOT_FOUND) -> 404
  · 02004 (UNAUTHORIZED_SESSION_ACCESS) -> 403
  · 02005 (CANNOT_REVOKE_CURRENT_SESSION) -> 400
```

---

## Task 4 — web (Angular): Action Logic

### Implement
```
1. Session Service:
   - Thêm method `revokeSession(sessionId: string): Observable<void>`.

2. Device List Component:
   - Thêm action handler cho nút "Đăng xuất" (Remote Logout).
   - Show confirm dialog trước khi gọi API.
   - Gọi `revokeSession(sessionId)`.
   - Success: 
     · Toast notify thành công.
     · Refresh danh sách thiết bị (gọi lại API list).
   - Error: Show toast báo lỗi.
```
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
- Styling Angular Material (MDC) Components: prioritize overriding CSS Custom Properties (variables) instead of direct properties. This is the modern approach for MDC-based components and avoids style conflicts.

## FILES TO RESTYLE
{paste danh sách file từ Task 4 output vào đây — mỗi file gồm .html + .css + context .ts}

## OUTPUT
Return complete updated file content for each file.
```
