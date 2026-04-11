# UC-023: UIElement Registry

## Tóm tắt
Admin quản lý danh sách UIElement — ánh xạ từng button/tab/menu item trên frontend sang resource:action tương ứng. Frontend dùng endpoint batch để biết element nào được hiển thị cho user hiện tại.

## Actor
BQL SUPER_ADMIN (CRUD), Authenticated User (batch query — read-only)

## Trạng thái
Planned

---

## UC-023-1: Tạo UIElement

**POST** `/api/v1/abac/ui-elements`

**Request**:
```json
{
  "elementId": "btn:employee:update",
  "label": "Edit Employee",
  "type": "BUTTON",
  "group": "employee-detail-actions",
  "orderIndex": 2,
  "resourceId": 1,
  "actionId": 4
}
```

**Invariant**: `elementId` unique trong toàn hệ thống. `resourceId` và `actionId` phải tồn tại, và `actionId` phải thuộc `resourceId`.

**Response**: `201 Created` — `UIElementView`

**Error**: `UI_ELEMENT_ID_DUPLICATE` → 409, `ACTION_NOT_FOUND` → 404

---

## UC-023-2: Lấy UIElement theo ID

**GET** `/api/v1/abac/ui-elements/{id}`

Response: `UIElementView` bao gồm `resourceName`, `actionName`

---

## UC-023-3: Danh sách UIElements

**GET** `/api/v1/abac/ui-elements?resourceId=&type=&group=&page=&size=`

Response: `Page<UIElementView>`

---

## UC-023-4: Cập nhật UIElement

**PUT** `/api/v1/abac/ui-elements/{id}`

Cho phép cập nhật: `label`, `type`, `group`, `orderIndex`, `resourceId`, `actionId`.

Không đổi `elementId` (frontend hardcode string này).

---

## UC-023-5: Xóa UIElement

**DELETE** `/api/v1/abac/ui-elements/{id}`

---

## UC-023-6: Batch Evaluate UIElements (Frontend API)

**POST** `/api/v1/abac/ui-elements/evaluate`

Dùng `AuthorizationContextEngine` để batch evaluate danh sách UIElement cho user hiện tại. Subject được lấy từ JWT của request.

**Request**:
```json
{
  "elementIds": [
    "btn:employee:create",
    "btn:employee:update",
    "btn:employee:delete",
    "tab:employee-detail:info"
  ]
}
```

**Response**:
```json
{
  "results": {
    "btn:employee:create": "DENY",
    "btn:employee:update": "PERMIT",
    "btn:employee:delete": "DENY",
    "tab:employee-detail:info": "PERMIT"
  }
}
```

**Flow**:
1. Load UIElement records theo `elementIds`
2. Build Subject từ JWT principal
3. Với mỗi UIElement: tạo `Action.semantic(actionName)` + `Resource(resourceName, null)` (navigation mode — không có instance data)
4. Gọi PdpEngine cho từng element
5. Trả map `elementId → decision`

**Auth**: JWT required. Subject = user đang login.

**Performance note**: Batch evaluate trong 1 request — không gọi PdpEngine N lần riêng lẻ. Load policy 1 lần, reuse cho tất cả elements.

---

## Response Models

### `UIElementView`
```json
{
  "id": 5,
  "elementId": "btn:employee:update",
  "label": "Edit Employee",
  "type": "BUTTON",
  "group": "employee-detail-actions",
  "orderIndex": 2,
  "resourceId": 1,
  "resourceName": "employee",
  "actionId": 4,
  "actionName": "UPDATE"
}
```

---

## Ghi chú thiết kế

- `elementId` dùng convention `{type}:{resource}:{action-slug}` — ví dụ `btn:employee:update`, `tab:user-detail:security`
- Batch evaluate (UC-023-6) là navigation-level evaluation: `object.data == null` tức là chỉ evaluate target expression + điều kiện không cần instance data
- Frontend gọi UC-023-6 một lần sau login để biết toàn bộ visibility state, không gọi per-button
- UIElement không có `isActive` flag — visibility được kiểm soát 100% bởi policy. Không có UIElement không có policy → default DENY
