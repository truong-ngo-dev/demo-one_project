# UC-027: Expression Composition (AND/OR)

## Tóm tắt
Mở rộng `ExpressionVO` từ LITERAL-only sang hỗ trợ COMPOSITION — kết hợp nhiều SpEL expressions bằng AND/OR. Admin có thể xây dựng điều kiện phức tạp hơn mà không cần viết 1 SpEL string dài, khó đọc.

## Actor
BQL SUPER_ADMIN (quản lý rule/policy expressions)

## Trạng thái
Planned

---

## Bối cảnh

Phase 1 chỉ hỗ trợ LITERAL:

```
ExpressionVO { id: 1, spelExpression: "subject.roles.contains('ADMIN') && resource.name == 'user'" }
```

COMPOSITION (đã có trong `abac_expression` schema — `combination_type`, `parent_id`) cho phép:

```
ExpressionVO (COMPOSITION, AND)
├── ExpressionVO (LITERAL): "subject.roles.contains('ADMIN')"
└── ExpressionVO (LITERAL): "resource.name == 'user'"
```

`libs/abac` đã có `Expression.Type.COMPOSITION` + `CombinationType` — chỉ cần implement ở admin service.

---

## UC-027-1: Cập nhật domain model

**`ExpressionVO`** mở rộng từ record sang sealed hierarchy:

```java
// Option A: Vẫn là record, thêm fields
public record ExpressionVO(
    Long id,
    Expression.Type type,           // LITERAL | COMPOSITION
    String spelExpression,          // non-null nếu LITERAL
    Expression.CombinationType combinationType,  // AND | OR, non-null nếu COMPOSITION
    List<ExpressionVO> subExpressions            // non-null nếu COMPOSITION
) { ... }
```

**Invariants**:
- `LITERAL` → `spelExpression` required, `subExpressions` null/empty.
- `COMPOSITION` → `combinationType` required, `subExpressions` min 2.
- `SpelValidator` chỉ validate LITERAL expressions.

---

## UC-027-2: DB Schema

Bảng `abac_expression` đã có cột `combination_type` và `parent_id`. Phase 2 dùng luôn, không cần migration mới.

```
abac_expression:
  id=1  type=COMPOSITION  combination_type=AND  parent_id=NULL  spel=NULL
  id=2  type=LITERAL      combination_type=NULL parent_id=1     spel="subject.roles.contains('ADMIN')"
  id=3  type=LITERAL      combination_type=NULL parent_id=1     spel="resource.name == 'user'"
```

---

## UC-027-3: Persistence thay đổi

**`AbacExpressionJpaEntity`** cần `parent_id` → `@ManyToOne(fetch=LAZY)` + `@OneToMany(mappedBy="parent")`.

**`PolicyPersistenceAdapter.upsertExpression()`** cần recursive save:
1. Save root expression
2. Save child expressions với parent_id = root.id

---

## UC-027-4: UI thay đổi (Rule editor)

Thay text area SpEL bằng Expression Builder:

```
[Expression Type: LITERAL ▼]
  [SpEL input]: ......................

[Expression Type: COMPOSITION ▼]
  Combine: [AND ▼]
  Sub-expressions:
    [+ Add expression]
    ├── LITERAL: "subject.roles.contains('ADMIN')"  [✕]
    └── LITERAL: "resource.name == 'user'"          [✕]
```

---

## Ghi chú thiết kế

- `libs/abac` `ExpressionEvaluators` đã xử lý COMPOSITION recursively — không cần sửa.
- UI Phase 2: chỉ hỗ trợ 1 cấp COMPOSITION (flat AND/OR). Nested composition là Phase 3.
- Backward compatible: toàn bộ Phase 1 LITERAL expressions không thay đổi.
