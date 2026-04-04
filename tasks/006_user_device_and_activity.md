# Task: User Device & Login History (User Self-Service)

## Trạng thái
- [x] Task 1 — UC-007 (oauth2-service): Implemented
- [x] Task 2 — UC-009 (oauth2-service): Implemented
- [x] Task 3 — OpenAPI contract: `docs/api/oauth2-sessions-history.yaml`
- [x] Task 4 — Angular logic: Implemented (Task 5 — Styling: dành cho Gemini)

## Definition of Done
- [ ] User xem được danh sách thiết bị + trạng thái session của chính mình.
- [ ] User xem được lịch sử đăng nhập của chính mình (phân trang).
- [ ] Angular hiển thị đúng: badge "Thiết bị này", nút "Đăng xuất" theo trạng thái session.
- [ ] Login history highlight các lần FAILED với IP/userAgent lạ.

---

## Task 1 — oauth2-service: UC-007 List Devices & Sessions

### Đọc trước
- `services/oauth2/CLAUDE.md`
- `docs/conventions/ddd-structure.md`
- `docs/conventions/error-handling.md`
- `services/oauth2/docs/domains/device.md`
- `services/oauth2/docs/domains/session.md`
- `services/oauth2/docs/use-cases/UC-007_list_devices.md`

### Implement
```
UC-007: User xem danh sách thiết bị — GET /api/v1/sessions/me

Luồng:
1. Đọc userId từ sub claim, sessionId từ sid claim của Access Token.
2. Query tất cả Device theo userId.
3. Join với OAuth Session (status = ACTIVE) để xác định session active.
4. So sánh sid từ token với sessionId của từng device → set isCurrent.
5. Trả về danh sách.

Response shape:
[
  {
    "deviceId": "uuid",
    "deviceName": "Chrome on macOS",       // detect từ User-Agent
    "ipAddress": "192.168.1.1",
    "lastSeenAt": "2025-01-01T00:00:00Z",
    "sessionId": "uuid | null",            // null nếu không có session active
    "sessionStatus": "ACTIVE | null",
    "isCurrent": true                      // so sánh sid claim
  }
]

Lưu ý:
- Device không có session active → sessionId = null, sessionStatus = null
- isCurrent chỉ true với đúng 1 item — device đang gọi request này
- Endpoint read-only — không thay đổi trạng thái hệ thống
```

---

## Task 2 — oauth2-service: UC-009 Login History

### Đọc trước
- `services/oauth2/CLAUDE.md`
- `docs/conventions/ddd-structure.md`
- `docs/conventions/error-handling.md`
- `services/oauth2/docs/domains/activity.md`
- `services/oauth2/docs/use-cases/UC-009_my_login_history.md`

### Implement
```
UC-009: User xem lịch sử đăng nhập — GET /api/v1/login-activities/me

Query params:
- page: int, default 0
- size: int, default 20, max 50

Luồng:
1. Đọc userId từ Access Token.
2. Query LoginActivity theo userId, sort createdAt DESC.
3. Trả về phân trang.

Response shape:
{
  "content": [
    {
      "result": "SUCCESS | FAILED_WRONG_PASSWORD | ...",
      "ipAddress": "string",
      "userAgent": "string",
      "provider": "LOCAL | GOOGLE | ...",
      "createdAt": "ISO8601",
      "deviceId": "uuid | null",      // null nếu login thất bại — device chưa được tạo
      "deviceName": "string | null"   // left join Device — null nếu deviceId null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3
}

Lưu ý:
- Không trả về username, hashedPassword hoặc bất kỳ field nhạy cảm nào
- deviceId nullable — FAILED attempts chưa có device → hiển thị "Thiết bị không xác định"
- Backend tự left join Device để lấy deviceName — Angular không gọi thêm endpoint
- Endpoint read-only — không thay đổi trạng thái hệ thống
```

---

## Task 3 — Generate API Contract

```
Dựa vào UC files sau, generate OpenAPI 3.0 spec (YAML):
- UC-007: GET /api/v1/sessions/me
- UC-009: GET /api/v1/login-activities/me

Include: query params, response shape, error codes, HTTP status.
Output: docs/api/oauth2-sessions-history.yaml
```

---

## Task 4 — web (Angular) — Logic

### Đọc trước
- `web/CLAUDE.md`
- `docs/api/oauth2-sessions-history.yaml`

### Implement
```
Implement logic cho Sessions & Login History.
Chỉ implement TypeScript — HTML là functional placeholder, không cần style.

1. Devices & Sessions page (/app/devices)
   - signals: devices (list), isLoading
   - Gọi GET /api/v1/sessions/me khi khởi tạo component
   - Mỗi device item expose:
     · isCurrent = true       → không có nút action, badge "Thiết bị này"
     · sessionStatus = ACTIVE → nút "Đăng xuất" → gọi UC-008 (nếu đã có)
     · sessionStatus = null   → không có nút action, label "Đã đăng xuất"
   - Sau khi revoke thành công → reload danh sách

2. Login History page (/app/login-history)
   - signals: activities (list), isLoading, page, totalPages
   - Gọi GET /api/v1/login-activities/me?page=&size=20
   - Pagination: nút Previous / Next
   - Mỗi item: hiển thị result, ipAddress, deviceName (hoặc "Thiết bị không xác định" nếu null), provider, createdAt
   - Flag "đáng chú ý": result != SUCCESS → mark để Task 5 highlight

3. Sidebar / Navigation — cập nhật
   - Thêm link "Thiết bị & Phiên đăng nhập" → /app/devices
   - Thêm link "Lịch sử đăng nhập" → /app/login-history
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
- Styling Angular Material (MDC) Components: prioritize overriding CSS Custom Properties (variables) instead of direct properties. This is the modern approach for MDC-based components and avoids style conflicts.

## FILES TO RESTYLE
{paste danh sách file từ Task 4 output vào đây — mỗi file gồm .html + .css + context .ts}

## NOTES
- Devices page: card mỗi device, badge "Thiết bị này" nổi bật, nút "Đăng xuất" màu warn
- Login history: table hoặc list, row result FAILED highlight màu warn/error
- Pagination: dùng Angular Material paginator nếu phù hợp

## OUTPUT
Return complete updated file content for each file.
```
