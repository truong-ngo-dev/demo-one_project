# Implementation Plan: ExpressionNode Promote-or-Reuse

**Mục tiêu:** Đảm bảo `ExpressionNode.Inline` không bao giờ được persist trực tiếp vào database. Mọi leaf expression phải được promote thành `NamedExpression` + `LibraryRef` trước khi lưu trữ.

---

## Task 1: Cập nhật JavaDoc trong ExpressionNode.java

**File:** `services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/domain/abac/policy/ExpressionNode.java`

### Tìm đoạn code:
```java
/** Leaf node containing a SpEL string directly. */
record Inline(String name, String spel) implements ExpressionNode {
    public boolean isNamed() { return name != null && !name.isBlank(); }
}
```

### Thay bằng:
```java
/**
 * Leaf node containing a SpEL string directly.
 * 
 * TRANSIENT ONLY — used in request/response mapping, never persisted directly to DB.
 * Application layer always promotes Inline → NamedExpression + LibraryRef before saving.
 * {@code name} is required when creating a new expression (used as NamedExpression name).
 */
record Inline(String name, String spel) implements ExpressionNode {
    public boolean isNamed() { return name != null && !name.isBlank(); }
}
```

---

## Task 2: Thêm validation trong ExpressionNodeRequest.java

**File:** `services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/presentation/abac/model/ExpressionNodeRequest.java`

### Tìm đoạn code trong method `toDomain()`:
```text
case "INLINE" -> new ExpressionNode.Inline(name, spel);
```

### Thay bằng:
```text
case "INLINE" -> {
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("INLINE expression requires a non-blank name");
    }
    if (spel == null || spel.isBlank()) {
        throw new IllegalArgumentException("INLINE expression requires a non-blank spel");
    }
    yield new ExpressionNode.Inline(name, spel);
}
```

---

## Task 3: Sửa ExpressionTreeService.java

**File:** `services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/application/expression/ExpressionTreeService.java`

### Task 3a: Sửa method `persist()` - thay thế case Inline
**Tìm:**
```text
return switch (node) {
    case ExpressionNode.Inline inline -> persistInline(inline, parentId);
    case ExpressionNode.LibraryRef ref -> persistLibraryRef(ref, parentId);
    case ExpressionNode.Composition comp -> persistComposition(comp, parentId);
};
```
**Thay bằng:**
```text
return switch (node) {
    case ExpressionNode.Inline inline -> promoteInline(inline, parentId);
    case ExpressionNode.LibraryRef ref -> persistLibraryRef(ref, parentId);
    case ExpressionNode.Composition comp -> persistComposition(comp, parentId);
};
```

### Task 3b: Sửa method `deleteTree()` - bỏ các case Inline logic cũ
**Tìm toàn bộ method `deleteTree()`:**
```java
public void deleteTree(Long rootId) {
    if (rootId == null) return;
    AbacExpressionJpaEntity row = expressionRepo.findById(rootId).orElse(null);
    if (row == null) return;

    if (row.getType() == AbacExpressionJpaEntity.ExpressionType.COMPOSITION) {
        List<AbacExpressionJpaEntity> children = expressionRepo.findAllByParentId(rootId);
        for (AbacExpressionJpaEntity child : children) {
            deleteTree(child.getId());
        }
        expressionRepo.delete(row);
    } else {
        // LITERAL
        if (row.getNamedExpressionId() != null) {
            // LibraryRef placeholder row — delete it but leave the NamedExpression AR
            expressionRepo.delete(row);
        } else if (row.getName() != null) {
            // Named inline — skip (may be reused elsewhere)
        } else {
            // Anonymous inline — delete
            expressionRepo.delete(row);
        }
    }
}
```
**Thay bằng:**
```java
public void deleteTree(Long rootId) {
    if (rootId == null) return;
    AbacExpressionJpaEntity row = expressionRepo.findById(rootId).orElse(null);
    if (row == null) return;

    if (row.getType() == AbacExpressionJpaEntity.ExpressionType.COMPOSITION) {
        List<AbacExpressionJpaEntity> children = expressionRepo.findAllByParentId(rootId);
        for (AbacExpressionJpaEntity child : children) {
            deleteTree(child.getId());
        }
        expressionRepo.delete(row);
    } else {
        // LITERAL — always a LibraryRef row. Delete the row, never the NamedExpression AR.
        expressionRepo.delete(row);
    }
}
```

### Task 3c: Sửa method `toNode()` - bỏ case Inline
**Tìm đoạn code ở cuối method `toNode()`:**
```text
// LITERAL
if (row.getNamedExpressionId() != null) {
    return new ExpressionNode.LibraryRef(NamedExpressionId.of(row.getNamedExpressionId()));
}
return new ExpressionNode.Inline(row.getName(), row.getSpelExpression());
```
**Thay bằng:**
```text
// LITERAL — always a LibraryRef (Inline is never persisted)
if (row.getNamedExpressionId() == null) {
    throw new IllegalStateException(
        "abac_expression LITERAL row id=" + row.getId() + " has no named_expression_id. " +
        "Inline rows must not exist in DB.");
}
return new ExpressionNode.LibraryRef(NamedExpressionId.of(row.getNamedExpressionId()));
```

### Task 3d: Xóa method `persistInline()`, thêm method `promoteInline()`
**Tìm toàn bộ method `persistInline()` và xóa nó:**
```java
private Long persistInline(ExpressionNode.Inline inline, Long parentId) {
    // For anonymous inlines at root level, attempt content dedup
    if (!inline.isNamed() && parentId == null) {
        var existing = expressionRepo.findBySpelExpressionAndParentIdIsNull(inline.spel());
        if (existing.isPresent()) return existing.get().getId();
    }
    AbacExpressionJpaEntity entity = new AbacExpressionJpaEntity();
    entity.setType(AbacExpressionJpaEntity.ExpressionType.LITERAL);
    entity.setSpelExpression(inline.spel());
    entity.setName(inline.isNamed() ? inline.name() : null);
    entity.setParentId(parentId);
    return expressionRepo.save(entity).getId();
}
```
**Thêm method mới vào cùng vị trí:**
```java
private Long promoteInline(ExpressionNode.Inline inline, Long parentId) {
    // Find existing NamedExpression by SpEL to avoid duplicates
    NamedExpression named = namedExpressionRepository.findBySpel(inline.spel())
        .orElseGet(() -> namedExpressionRepository.save(
            NamedExpression.create(inline.name(), inline.spel())));
    
    // Persist as LibraryRef
    return persistLibraryRef(new ExpressionNode.LibraryRef(named.getId()), parentId);
}
```

---

## Task 4: Xóa method không còn dùng trong AbacExpressionJpaRepository.java

**File:** `services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/infrastructure/persistence/abac/expression/AbacExpressionJpaRepository.java`

### Tìm dòng code sau và xóa nó:
```java
Optional<AbacExpressionJpaEntity> findBySpelExpressionAndParentIdIsNull(String spelExpression);
```

---

## Task 5: Kiểm tra Compile

Sau khi hoàn thành các thay đổi trên, thực hiện kiểm tra lỗi compile:
```bash
./mvnw compile -pl services/admin
```
*Lưu ý: Nếu có lỗi compile liên quan đến `findBySpelExpressionAndParentIdIsNull` ở các file khác, hãy xóa các dòng gọi method đó.*

---

## Tóm tắt các file thay đổi

| File                                                                          | Loại thay đổi                   |
|:------------------------------------------------------------------------------|:--------------------------------|
| `domain/abac/policy/ExpressionNode.java`                                      | JavaDoc only                    |
| `presentation/abac/model/ExpressionNodeRequest.java`                          | Thêm validation (name required) |
| `application/expression/ExpressionTreeService.java`                           | Logic thay đổi lớn              |
| `infrastructure/persistence/abac/expression/AbacExpressionJpaRepository.java` | Xóa 1 method                    |

**Không cần sửa:** `ExpressionNodeView.java`, `NamedExpression.java`, `AbacExpressionJpaEntity.java`, migration files.
