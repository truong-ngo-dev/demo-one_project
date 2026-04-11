# UC-030 — Navigation Simulate

## Mô tả
Admin hoặc SUPER_ADMIN nhập một virtual subject (roles, attributes) và chọn một resource.
Hệ thống đánh giá toàn bộ actions đã đăng ký của resource đó ở **navigation level** (không có instance data)
và trả về danh sách kết quả PERMIT / DENY theo từng action.

Dùng để kiểm tra: "Với role MANAGER, action nào trên resource employee được phép?"

## Actor
Admin, SUPER_ADMIN

## Điều kiện tiên quyết
- Resource đã được khai báo trong Resource Catalogue (UC-019)
- Resource có ít nhất một ActionDefinition

## Flow chính
1. Admin gửi `POST /api/v1/abac/simulate/navigation` với subject + resourceName
2. Hệ thống tìm ResourceDefinition theo resourceName
3. Load root PolicySet (hoặc theo policySetId nếu truyền)
4. Build Subject từ request
5. Với mỗi ActionDefinition của resource: gọi PdpEngine.authorize() với `object.data = null`
6. Trả về danh sách `{ action, decision }` sort theo action name

## Input

```json
POST /api/v1/abac/simulate/navigation
{
  "subject": {
    "userId": "optional-user-id",
    "roles": ["MANAGER"],
    "attributes": { "managedDepartments": ["engineering"] }
  },
  "resourceName": "employee",
  "policySetId": null
}
```

| Field | Required | Mô tả |
|-------|----------|-------|
| subject.userId | No | userId tùy chọn |
| subject.roles | Yes | Danh sách roles của virtual subject |
| subject.attributes | No | Attributes tùy chọn (Map<String, Object>) |
| resourceName | Yes | Tên resource đã khai báo trong catalogue |
| policySetId | No | null → dùng root PolicySet |

## Output

```json
{
  "data": {
    "resourceName": "employee",
    "policySetId": 1,
    "policySetName": "bql-root",
    "decisions": [
      { "action": "CREATE", "decision": "DENY" },
      { "action": "DELETE", "decision": "DENY" },
      { "action": "LIST",   "decision": "PERMIT" },
      { "action": "LOCK",   "decision": "DENY" },
      { "action": "READ",   "decision": "PERMIT" },
      { "action": "UPDATE", "decision": "DENY" }
    ]
  }
}
```

`decisions` được sort alphabetically theo action name.

## Error Cases

| Điều kiện | HTTP | Error |
|-----------|------|-------|
| resourceName không tồn tại | 404 | RESOURCE_NOT_FOUND |
| policySetId truyền nhưng không tìm thấy | 404 | POLICY_SET_NOT_FOUND |

## Known Limitations (Phase 2)
- `decision` chỉ trả `PERMIT` / `DENY` — không trả rule name hay trace (Phase 3)
- Navigation level: `object.data = null` — instance-level conditions trong rule luôn evaluate theo logic SpEL
  (nếu rule có `object.data == null` branch thì vẫn PERMIT ở navigation level)
- `policySetId` trong response là metadata tham khảo — engine luôn load từ root PolicySet
  (multi-policySet routing là Phase 2 enforcement, không phải Phase 2 admin console)
