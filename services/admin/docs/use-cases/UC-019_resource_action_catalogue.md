# UC-019: Resource & Action Catalogue

## Tóm tắt
Admin quản lý danh mục Resource và Action — nền tảng để viết policy và khai báo UIElement.

## Actor
BQL SUPER_ADMIN

## Trạng thái
Planned

---

## Resource CRUD

### UC-019-R1: Tạo Resource

**Actor**: SUPER_ADMIN  
**Trigger**: POST `/api/v1/abac/resources`

**Request**:
```json
{
  "name": "employee",
  "description": "Hồ sơ nhân viên",
  "serviceName": "admin-service"
}
```

**Flow**:
1. Validate `name` không trống, không trùng với resource đã tồn tại
2. Tạo `ResourceDefinition` aggregate
3. Persist

**Response**: `201 Created` — `ResourceDefinitionView`

**Error**: `RESOURCE_NAME_DUPLICATE` → 409

---

### UC-019-R2: Lấy Resource theo ID

**GET** `/api/v1/abac/resources/{id}`

Response: `ResourceDefinitionView` bao gồm danh sách `actions`

---

### UC-019-R3: Danh sách Resource

**GET** `/api/v1/abac/resources?keyword=&page=&size=`

Response: `Page<ResourceSummaryView>` — name, serviceName, actionCount, uiElementCount

---

### UC-019-R4: Cập nhật Resource

**PUT** `/api/v1/abac/resources/{id}`

Cho phép cập nhật `description`, `serviceName`. Không đổi được `name` (tránh break SpEL references).

---

### UC-019-R5: Xóa Resource

**DELETE** `/api/v1/abac/resources/{id}`

Guard: Kiểm tra không có Policy hoặc UIElement nào đang tham chiếu resource này.

**Error**: `RESOURCE_IN_USE` → 409

---

## Action CRUD (sub-resource của Resource)

### UC-019-A1: Thêm Action vào Resource

**POST** `/api/v1/abac/resources/{resourceId}/actions`

**Request**:
```json
{
  "name": "LOCK",
  "description": "Khóa nhân viên",
  "isStandard": false
}
```

**Invariant**: `name` unique trong cùng resource. Standard actions: LIST, READ, CREATE, UPDATE, DELETE.

---

### UC-019-A2: Cập nhật Action

**PATCH** `/api/v1/abac/resources/{resourceId}/actions/{actionId}`

Cho phép cập nhật `description`. Không đổi `name` (tránh break SpEL).

---

### UC-019-A3: Xóa Action

**DELETE** `/api/v1/abac/resources/{resourceId}/actions/{actionId}`

Guard: Không xóa nếu có UIElement tham chiếu action này.

---

## Response Models

### `ResourceDefinitionView`
```json
{
  "id": 1,
  "name": "employee",
  "description": "Hồ sơ nhân viên",
  "serviceName": "admin-service",
  "actions": [
    { "id": 1, "name": "LIST", "description": "...", "isStandard": true },
    { "id": 6, "name": "LOCK", "description": "...", "isStandard": false }
  ]
}
```

### `ResourceSummaryView`
```json
{
  "id": 1,
  "name": "employee",
  "serviceName": "admin-service",
  "actionCount": 6,
  "uiElementCount": 4
}
```

---

## Ghi chú thiết kế

- Action `name` không cho phép edit sau khi tạo để tránh làm invalid SpEL expressions đang dùng `action.getAttribute('name') == 'LOCK'`
- Tương tự `resource.name` — immutable sau khi tạo
- Standard actions (LIST/READ/CREATE/UPDATE/DELETE) nên được seed sẵn khi tạo resource mới (checkbox "Add standard CRUD actions")
