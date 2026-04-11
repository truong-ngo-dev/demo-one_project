# UC-036 — UIElement Policy Coverage

## Mô tả

Đánh dấu mỗi UIElement có hay không được cover bởi ít nhất một PERMIT rule trong hệ thống.
Admin có thể xem trạng thái coverage trên từng element và lấy danh sách các element chưa được cover.

---

## Actors

- **Admin** — xem danh sách UIElement với trạng thái coverage, query uncovered list

---

## Coverage Logic

Coverage check thực hiện tại query time:

1. Load all root PolicySets (`PolicySetRepository.findAllRoot()`)
2. Với mỗi PolicySet, load tất cả Policies (`PolicyRepository.findByPolicySetId()`)
3. Với mỗi Policy, iterate Rules
4. Với mỗi PERMIT Rule, chạy `SpelExpressionAnalyzer.analyze(targetExpression, conditionExpression)`
5. **Covered nếu:** `specificActions.isEmpty()` (wildcard — covers all actions) **hoặc** `specificActions.contains(actionName)`
6. UIElement được coi là covered nếu tìm thấy ít nhất 1 PERMIT rule thỏa mãn

---

## API Changes

### `GET /api/v1/abac/ui-elements/{id}`

Response `UIElementView` bổ sung field:
```json
{
  "hasPolicyCoverage": true
}
```

### `GET /api/v1/abac/ui-elements`

Response `UIElementSummary` bổ sung field:
```json
{
  "hasPolicyCoverage": false
}
```

### `GET /api/v1/abac/ui-elements/uncovered`

Trả về danh sách tất cả UIElement chưa được cover bởi bất kỳ PERMIT rule nào.

**Response:** `List<UncoveredUIElement>` gồm: `id, elementId, label, type, elementGroup, resourceId, resourceName, actionId, actionName`

---

## Use Cases

| Use case                   | Class                                                                       |
|----------------------------|-----------------------------------------------------------------------------|
| Get single element         | `application.ui_element.query.get_ui_element.GetUIElement`                 |
| List elements (paginated)  | `application.ui_element.query.list_ui_elements.ListUIElements`             |
| List uncovered elements    | `application.ui_element.query.list_uncovered_ui_elements.ListUncoveredUIElements` |

---

## Performance Note

Coverage index được tính once per request trong `ListUIElements` (`buildCoverageIndex()`).
Với codebase lớn, có thể cache kết quả nếu cần tối ưu thêm.
