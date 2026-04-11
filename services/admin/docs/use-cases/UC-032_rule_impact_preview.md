# UC-032 — Rule Impact Preview

## Mô tả

Trước khi lưu `targetExpression` / `conditionExpression` của một rule, admin có thể gọi endpoint này để phân tích biểu thức SpEL và nhận về:
- Các **roles** bắt buộc (trích từ `subject.roles.contains('X')`)
- Các **subject attributes** cần thiết (trích từ `subject.getAttribute('X')`)
- Các **actions** cụ thể mà rule áp dụng (trích từ `action.getAttribute('name') == 'X'`)
- Liệu rule có hỗ trợ **navigation level** (không cần object data)
- Cảnh báo nếu expression quá phức tạp để phân tích

Đây là stateless analysis — không query database, không side effect.

---

## Endpoint

```
POST /api/v1/abac/rules/impact-preview
```

### Request Body

```json
{
  "targetExpression":    "subject.roles.contains('MANAGER') && action.getAttribute('name') == 'READ'",
  "conditionExpression": "object.data == null || object.data.department == subject.getAttribute('dept')"
}
```

Cả hai field đều optional (null → bỏ qua trong phân tích).

### Response

```json
{
  "data": {
    "requiredRoles":       ["MANAGER"],
    "requiredAttributes":  ["dept"],
    "specificActions":     ["READ"],
    "navigableWithoutData": true,
    "hasInstanceCondition": true,
    "parseWarning":         null
  }
}
```

| Field                  | Ý nghĩa                                                                                 |
|------------------------|-----------------------------------------------------------------------------------------|
| `requiredRoles`        | Roles trích được từ `subject.roles.contains('X')` literals                              |
| `requiredAttributes`   | Attribute keys trích từ `subject.getAttribute('X')` literals                            |
| `specificActions`      | Action names trích từ `action.getAttribute('name') == 'X'` literals                     |
| `navigableWithoutData` | `true` nếu có `object.data == null` branch → rule hoạt động ở navigation level          |
| `hasInstanceCondition` | `true` nếu có access vào `object.data.field` → rule cần instance data để đánh giá đúng |
| `parseWarning`         | `null` nếu OK; có message nếu SpEL parse thất bại                                       |

---

## Known Limitations

- **Literal-only**: Chỉ detect patterns có string literal hardcoded. Expressions dynamic như `subject.roles.contains(someVar)` không được phát hiện.
- **No user count**: Không đếm được số users bị ảnh hưởng (cần query DB theo role/attribute — Phase 3).
- **No reverse lookup**: Không tra ra resource/policy nào sẽ thay đổi behavior khi rule thay đổi.
- **AST depth**: Walker chỉ detect patterns ở top-level của CompoundExpression. Nested conditions inside sub-expressions (e.g., trong ternary, chained maps) có thể bị bỏ qua.

---

## Trạng thái

| Trường      | Giá trị                                                               |
|-------------|-----------------------------------------------------------------------|
| Trạng thái  | Implemented                                                           |
| Handler     | `application/rule/query/impact_preview/GetRuleImpactPreview.Handler` |
| Controller  | `presentation/abac/RuleImpactController`                              |
| Phase       | Phase 2 — Admin Console Usability                                     |
