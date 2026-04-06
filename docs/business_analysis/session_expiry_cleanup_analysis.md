# Session Expiry & Cleanup — Phân tích chi tiết theo từng Case

## Kiến trúc reference

```
[Browser] → SESSION cookie → [Gateway Redis] → tokens → [oauth2-service]
                                   ↕
                         webgw:oauth:{authId} ↔ gateway_session_id

[oauth2-service]
  oauth_sessions:  authorizationId, idpSessionId, status
  Spring AS:       OAuth2Authorization (tokens + expiry)
  JDBC Session:    idpSessionId
```

---

## TTL Strategy

### Bối cảnh: IdP session và Refresh token là hai thứ độc lập

**Token refresh** (`grant_type=refresh_token`) là **server-to-server call** từ Gateway đến oauth2-service — không cần browser, không cần IdP HTTP session còn sống. Spring AS chỉ validate refresh token trong DB.

IdP HTTP session chỉ cần thiết khi browser đi qua **OAuth2 Authorization Code Flow** (login page). Sau khi login xong, IdP session chết hay sống không ảnh hưởng đến khả năng Gateway refresh token.

**SSO** chỉ phát huy khi có **nhiều client độc lập** cùng trust một IdP. Ví dụ:
```
person.apartcom.vn  ─┐
                     ├─ cùng IdP → login 1 lần, dùng được cả hai
org.apartcom.vn     ─┘
```
Nếu chỉ có một client (Web Gateway), SSO không có ý nghĩa thực tế.

---

### Strategy A — Decouple (không cần SSO) ← **Đang dùng**

```
IdP JDBC session TTL:  1–2 giờ
Refresh token TTL:     7 ngày
reuseRefreshTokens:    false   (rotating)
Gateway Redis TTL:     30 phút idle
```

**Khi nào dùng**: Project chỉ có một Web Gateway, không có nhiều subdomain/phân hệ cùng IdP.

**Expiry trigger cho Case 3**: Push từ Gateway khi nhận `invalid_grant` + cron [PLANNED] cho user abandon.

**Nhược điểm**: `SessionDestroyedEvent` không dùng làm expiry trigger (IdP session chết trước refresh token). Cần push endpoint riêng.

---

### Strategy B — Align TTL (có SSO) ← **Dùng khi có multi-client**

```
IdP JDBC session TTL:  bằng Refresh token TTL (7 ngày)
Refresh token TTL:     7 ngày
reuseRefreshTokens:    false   (rotating)
Gateway Redis TTL:     30 phút idle
```

**Khi nào dùng**: Khi có phân hệ thứ hai (ví dụ `org.apartcom.vn`) dùng cùng IdP — lúc đó SSO phải hoạt động xuyên suốt 7 ngày.

**Expiry trigger cho Case 3**: `SessionDestroyedEvent` đủ điều kiện làm primary trigger vì TTL align → không cần push endpoint.

**Nhược điểm**: JDBC session rows sống 7 ngày → cần Spring Session JDBC cleanup job chạy thường xuyên.

---

### Migration từ A → B

Khi có client thứ hai, việc chuyển đổi gọn:
1. Align `server.servlet.session.timeout = refresh-token-ttl`
2. Upgrade `SessionDestroyedEvent` handler: từ "null `idpSessionId`" thành full `expire()` flow (xem Case 4)
3. Xóa push endpoint `/sessions/expire` trên oauth2-service và Gateway handler tương ứng
4. Thêm Spring Session JDBC cleanup job

---

## Case 1 — Explicit Logout

**Trigger**: User click logout → `POST /webgw/auth/logout`

**Flow**:
```
Gateway invalidate Redis session
  → redirect browser đến IdP logout endpoint
  → AuthorizationRevokingLogoutSuccessHandler
      → RevokeSession → Oauth2Session.revoke() → SessionRevokedEvent
      → RevocationNotifier.notify(authorizationId) → Gateway xóa Redis mapping
  → IdP JDBC session destroyed → SessionDestroyedEvent (side effect, idempotent)
```

| Strategy | Việc cần làm |
|----------|-------------|
| **A** | Không cần thay đổi — đã implemented ✓ |
| **B** | Không cần thay đổi — đã implemented ✓ |

---

## Case 2 — Access Token hết hạn, Refresh Token còn sống

**Trigger**: User gọi API → Resource Server trả 401 → Gateway refresh

**Flow**:
```
Gateway detect 401
  → gọi token endpoint: grant_type=refresh_token
  → Spring AS issue access token mới (+ refresh token mới vì rotating)
  → Gateway update Redis session với token mới
  → retry request → 200
```

`sid` = `authorizationId` không đổi trong suốt refresh cycle → mapping keys không cần cập nhật.

| Strategy | Việc cần làm |
|----------|-------------|
| **A** | Không cần thay đổi — đã implemented ✓ |
| **B** | Không cần thay đổi — đã implemented ✓ |

---

## Case 3 — Refresh Token hết hạn (Session thực sự hết hạn)

**Trigger**: Gateway gọi token endpoint → Spring AS trả `400 invalid_grant`

**Trạng thái tại thời điểm này**:
- `oauth_sessions`: vẫn ACTIVE ← cần cleanup
- Spring AS `OAuth2Authorization`: stale record còn trong DB
- Gateway Redis: session + mapping có thể vẫn còn ← rác

---

**Strategy A — cần implement:**

```
[Gateway] Nhận invalid_grant
  → xóa Gateway Redis session
  → POST /api/oauth2/v1/sessions/expire { authorizationId }   ← endpoint mới
  → redirect user về login

[oauth2-service] Nhận expire request
  → lookup Oauth2Session by authorizationId
  → session.expire() → SessionExpiredEvent
  → SessionExpiredEventHandler:
      ├── SessionTerminationService.terminate(authorizationId)
      └── RevocationNotifier.notify(authorizationId) → Gateway xóa mapping keys
```

Cron [PLANNED]: sweep `oauth_sessions` ACTIVE mà `authorizationId` không còn trong `oauth2_authorization` — cover user abandon (Gateway TTL hết trước, không có `invalid_grant` nào được trigger).

---

**Strategy B — cần implement:**

Không cần push endpoint. `SessionDestroyedEvent` đảm nhận vai trò trigger vì TTL align:

```
IdP JDBC session hết (= refresh token cũng hết)
  → SessionDestroyedEvent(idpSessionId)
  → lookup Oauth2Session by idpSessionId
  → session.expire() → SessionExpiredEvent
  → SessionExpiredEventHandler:
      ├── SessionTerminationService.terminate(authorizationId)
      └── RevocationNotifier.notify(authorizationId) → Gateway xóa mapping keys
```

Cron [PLANNED]: vẫn giữ làm defense-in-depth.

---

## Case 4 — IdP JDBC Session hết hạn (Refresh Token còn sống)

**Trigger**: Spring Session JDBC TTL hết → `SessionDestroyedEvent(idpSessionId)`

---

**Strategy A — cần implement:**

Refresh token vẫn sống → chỉ clean stale reference, không expire:

```
SessionDestroyedEvent(idpSessionId)
  → lookup Oauth2Session by idpSessionId
  → oauth_sessions.idpSessionId = null
  → KHÔNG expire(), KHÔNG notify Gateway
```

Lý do null `idpSessionId`: tránh Spring Session reuse ID gây lookup nhầm về sau.

---

**Strategy B — không xảy ra case này:**

Vì TTL align, IdP session và refresh token hết cùng lúc → `SessionDestroyedEvent` đồng nghĩa với Case 3. Handler xử lý full expire() flow thay vì chỉ null reference.

```
SessionDestroyedEvent(idpSessionId)
  → lookup Oauth2Session by idpSessionId
  → session.expire() → SessionExpiredEvent   ← full expire, không phải null
  → SessionExpiredEventHandler:
      ├── SessionTerminationService.terminate(authorizationId)
      └── RevocationNotifier.notify(authorizationId)
```

---

## Case 5 — Gateway Redis Session hết TTL (Tokens còn sống)

**Trigger**: Spring Session Redis idle TTL hết → Redis tự xóa session

**Trạng thái**:
- Gateway Redis session: gone
- `webgw:oauth:{authId}` và `webgw:session:{sid}`: orphan nếu mapping TTL không align
- `oauth_sessions`: ACTIVE, không đổi
- Tokens tại Spring AS: vẫn valid

**Flow khi user quay lại**:
```
User request → không có SESSION cookie hợp lệ
  → Gateway khởi tạo OAuth2 flow → dùng lại refresh token → tạo session mới
  → user tiếp tục bình thường (transparent)
```

**Cleanup orphan mapping keys:**
```
SessionDeletedEvent (Spring Session Redis)
  → xóa webgw:oauth:{authId} và webgw:session:{sid} nếu còn
```

| Strategy | Việc cần làm |
|----------|-------------|
| **A** | Implement `SessionDeletedEvent` listener tại Gateway — xóa orphan mapping keys |
| **B** | Như Strategy A — không thay đổi |

Lý do KHÔNG notify oauth2-service: `oauth_sessions` vẫn đúng (ACTIVE, tokens valid). Không có gì sai ở oauth2 side.

---

## Case 6 — Refresh Token bị steal + Rotation Detection

**Trigger**: Kẻ tấn công dùng stolen refresh token → Spring AS phát hiện reuse (`reuseRefreshTokens = false`) → revoke toàn bộ token family → trả `400 invalid_grant`

Gateway nhận `invalid_grant` — cùng signal với Case 3 nhưng Spring AS đã tự revoke authorization. Cần phân biệt để xử lý đúng severity:

```
[Gateway] Nhận invalid_grant
  → POST /api/oauth2/v1/sessions/expire { authorizationId }
      (oauth2-service tự detect authorization đã bị revoke bởi Spring AS
       → dùng revoke() thay vì expire() nếu authorization không còn tồn tại)
  → redirect user về login

[oauth2-service]
  → lookup Oauth2Session by authorizationId
  → nếu OAuth2Authorization không còn tồn tại (đã bị Spring AS revoke):
      session.revoke() → SessionRevokedEvent     ← revoke, không phải expire
  → SessionRevokedEventHandler:
      ├── SessionTerminationService.terminate(authorizationId)  (no-op, đã revoke)
      └── RevocationNotifier.notify(authorizationId) → Gateway xóa mapping keys
  → Audit log: "[SECURITY] refresh token reuse detected — userId={}, authorizationId={}"
```

| Strategy | Việc cần làm |
|----------|-------------|
| **A** | Cùng push endpoint với Case 3. oauth2-service phân biệt expire vs revoke dựa trên trạng thái authorization. Thêm audit log |
| **B** | Như Strategy A — `SessionDestroyedEvent` không cover case này (không có session destroy). Push endpoint vẫn cần |

Lý do quan trọng: Nếu không revoke và không notify Gateway, kẻ tấn công có thể vẫn dùng được Gateway session hiện tại (nếu chưa hết TTL) dù refresh token đã bị revoke.

---

## Case 7 — Admin Force Terminate

**Trigger**: Admin gọi `DELETE /api/v1/sessions/admin/{sessionId}`

**Flow hiện tại**:
```
AdminRevokeSession
  → SessionTerminationService.terminate(authorizationId)
  → Oauth2Session.revoke() → SessionRevokedEvent
  → RevocationNotifier.notify(authorizationId) → Gateway xóa Redis
  + AUDIT log
```

| Strategy | Việc cần làm |
|----------|-------------|
| **A** | Không cần thay đổi — đã implemented ✓ |
| **B** | Không cần thay đổi — đã implemented ✓ |

Gap nhỏ chấp nhận được: access token đang còn hạn vẫn pass đến hết hạn (JWT stateless trade-off — không thể tránh nếu không dùng introspection).

---

## Case 8 — User bị Lock trong khi Session đang active

**Trigger**: Admin lock user → `UserLockedEvent` tại admin-service

**Flow cần implement**:
```
UserLockedEvent (admin-service)
  → POST /api/oauth2/v1/internal/users/{userId}/sessions/revoke-all  ← endpoint mới
  → oauth2-service: find all ACTIVE oauth_sessions by userId
      → foreach:
          revoke() → SessionRevokedEvent
          → SessionTerminationService.terminate(authorizationId)
          → RevocationNotifier.notify(authorizationId) → Gateway xóa Redis
```

| Strategy | Việc cần làm |
|----------|-------------|
| **A** | Implement `UserLockedEvent` handler + endpoint `revoke-all` trên oauth2-service |
| **B** | Như Strategy A — không thay đổi |

Security gap nghiêm trọng nhất: nếu không implement, user bị lock vẫn dùng hệ thống 7 ngày.

---

## Tổng hợp

### Gap Analysis — Strategy A (hiện tại)

| Case | Status | Việc cần làm |
|------|--------|-------------|
| 1. Explicit logout | Implemented ✓ | — |
| 2. Access token expired | Implemented ✓ | — |
| 3. Refresh token expired | **Gap** | Push endpoint `/sessions/expire` trên oauth2-service + Gateway xử lý `invalid_grant` + Cron [PLANNED] |
| 4. IdP session expired | **Gap** | `SessionDestroyedEvent` listener → null `idpSessionId` |
| 5. Gateway Redis TTL | **Gap** | `SessionDeletedEvent` listener tại Gateway → xóa orphan mapping keys |
| 6. Refresh token stolen | **Gap** | Tái dùng push endpoint Case 3 + detect revoke vs expire + audit log |
| 7. Admin force terminate | Implemented ✓ | — |
| 8. User locked | **Gap** | `UserLockedEvent` handler + endpoint `revoke-all` |

### Gap Analysis — Strategy B (khi có multi-client SSO)

| Case | Thay đổi so với A |
|------|------------------|
| 1. Explicit logout | Không đổi |
| 2. Access token expired | Không đổi |
| 3. Refresh token expired | Xóa push endpoint. `SessionDestroyedEvent` thay thế làm trigger |
| 4. IdP session expired | Upgrade handler: full `expire()` thay vì chỉ null `idpSessionId` |
| 5. Gateway Redis TTL | Không đổi |
| 6. Refresh token stolen | Không đổi (push vẫn cần, `SessionDestroyedEvent` không cover) |
| 7. Admin force terminate | Không đổi |
| 8. User locked | Không đổi |
