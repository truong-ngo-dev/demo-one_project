# Expression System — Design Specification

*2026-04-14*

---

## 1. Bối cảnh

Thiết kế này thay thế `ExpressionVO(id, spElExpression)` hiện tại — vốn chỉ là flat SpEL string — bằng một expression tree có thể tái sử dụng và thân thiện với người dùng hơn.

Vấn đề cốt lõi cần giải quyết:
- Các expression phổ biến như `subject.roles.contains('BQL_MANAGER')` lặp lại ở hàng chục rules → khó maintain
- Người dùng cần đọc và hiểu expression mà không cần biết SpEL
- Phải support cả visual builder lẫn raw SpEL để cover mọi use case

---

## 2. Node types

Expression được biểu diễn dưới dạng cây. Mỗi node là một trong ba loại:

```
Inline      — leaf node, chứa SpEL trực tiếp (anonymous hoặc có name)
LibraryRef  — leaf node, tham chiếu đến NamedExpression bằng ID
Composition — AND | OR block, chứa danh sách child nodes
```

| Node        | Identity                       | Owned by         | Shared                        |
|-------------|--------------------------------|------------------|-------------------------------|
| Inline      | Không có domain identity       | Rule/Policy tree | Không                         |
| LibraryRef  | Tham chiếu `NamedExpressionId` | Không owned      | Có — nhiều rules dùng cùng AR |
| Composition | Không có domain identity       | Rule/Policy tree | Không                         |

---

## 3. Domain model

### Package `domain/abac/expression/` — mới

`NamedExpression` là Aggregate Root độc lập, có lifecycle riêng, không bị cascade bởi Policy hay Rule.

```java
// Aggregate Root
public class NamedExpression {
    NamedExpressionId id;
    String name;
    String spel;

    public static NamedExpression create(String name, String spel) { /* body */ }
    public NamedExpression rename(String newName) { /* body */ }
    public NamedExpression updateSpel(String newSpel) { /* body */ }
}

public record NamedExpressionId(Long value) {
    public static NamedExpressionId of(Long value) { /* body */ }
}

public interface NamedExpressionRepository {
    Optional<NamedExpression> findById(NamedExpressionId id);
    Optional<NamedExpression> findBySpel(String spel);
    List<NamedExpression> findAll();
    NamedExpression save(NamedExpression expr);
    void delete(NamedExpressionId id);
    boolean isInUse(NamedExpressionId id);   // check trước khi delete
}
```

### Package `domain/abac/policy/` — cập nhật

`ExpressionNode` là Value Object thuần — **không chứa DB id**, domain không biết về persistence.

```java
public sealed interface ExpressionNode
    permits ExpressionNode.Inline, ExpressionNode.LibraryRef, ExpressionNode.Composition {

    // Inline: TRANSIENT ONLY — dùng trong request/response, không bao giờ được persist trực tiếp.
    // App layer luôn promote Inline → NamedExpression + LibraryRef trước khi lưu DB.
    // name là bắt buộc khi tạo mới (để đặt tên cho NamedExpression sẽ được tạo).
    record Inline(String name, String spel) implements ExpressionNode {
        public boolean isNamed() { return name != null && !name.isBlank(); }
    }

    // LibraryRef: cross-aggregate reference by ID — đúng DDD.
    // Đây là dạng leaf DUY NHẤT được persist trong abac_expression.
    record LibraryRef(NamedExpressionId refId) implements ExpressionNode {}

    // Composition: AND/OR block, structural only
    record Composition(CombinationType operator,
                       List<ExpressionNode> children) implements ExpressionNode {}
}
```

`ExpressionVO` bị xóa hoàn toàn. `RuleDefinition` và `PolicyDefinition` dùng `ExpressionNode`.

> **Quy tắc cốt lõi**: Trong DB không bao giờ tồn tại `Inline` row. Mọi leaf expression đều là `LibraryRef` trỏ vào `NamedExpression`. `Inline` chỉ sống trong request payload và được promote ngay tại persist time.

### Cấu trúc package kết quả

```
domain/abac/expression/
  NamedExpression.java           ← Aggregate Root
  NamedExpressionId.java         ← Value Object
  NamedExpressionRepository.java ← domain interface

domain/abac/policy/
  ExpressionNode.java            ← sealed VO (Inline | LibraryRef | Composition)
  RuleDefinition.java            ← targetExpression, conditionExpression: ExpressionNode
  PolicyDefinition.java          ← targetExpression: ExpressionNode
  ExpressionVO.java              ← XÓA
```

---

## 4. Schema thay đổi

### Bảng mới: `named_expression`

`NamedExpression` AR có bảng riêng — tách bạch khỏi structural tree nodes.

```sql
CREATE TABLE named_expression (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(200) NOT NULL UNIQUE,
    spel  TEXT NOT NULL
);
```

### Bảng `abac_expression` — chỉ chứa tree nodes

Bảng này chứa LibraryRef và Composition nodes. Thêm `named_expression_id` để biểu diễn LibraryRef:

```sql
ALTER TABLE abac_expression
  ADD COLUMN name                 VARCHAR(200) NULL,
  ADD COLUMN named_expression_id  BIGINT       NULL,
  ADD CONSTRAINT fk_expr_named
    FOREIGN KEY (named_expression_id) REFERENCES named_expression(id);
```

Mapping:

| Domain node | type        | spel_expression | named_expression_id | name |
|-------------|-------------|-----------------|---------------------|------|
| LibraryRef  | LITERAL     | null            | có                  | null |
| Composition | COMPOSITION | null            | null                | null |

> **Lưu ý**: `Inline` node **không bao giờ được persist**. Cột `spel_expression` và `name` trong `abac_expression` không còn được dùng cho LITERAL rows — giữ lại trong schema để không breaking migration, nhưng luôn null. Mọi leaf expression đều đi qua `NamedExpression` AR.

Migration file: `V8__named_expression_and_tree_ref.sql`

---

## 5. Trách nhiệm từng tầng

```
Domain layer:
  NamedExpression AR     — create, rename, updateSpel
  NamedExpressionRepository — findBySpel (dùng bởi find-or-create ở app layer)
  ExpressionNode VO      — biểu diễn cây thuần, không biết DB id

Application layer:
  ExpressionTreeService  — orchestrate persist + resolve
    • persist(ExpressionNode) → gọi persistence adapter, trả về node với DB context
    • resolveSpel(node) → expand LibraryRef qua NamedExpressionRepository
    • deleteTree(rootDbId) → selective delete (skip LibraryRef rows)
  ListNamedExpressions   — dùng NamedExpressionRepository.findAll()
  DeleteNamedExpression  — check isInUse() trước khi delete

Infrastructure layer:
  ExpressionPersistenceAdapter — chịu hoàn toàn về:
    • find-or-create Inline rows trong abac_expression
    • adjacency list cho Composition nodes (parentId tracking)
    • DB id không expose lên domain
    • reconstruct ExpressionNode tree từ DB rows
  NamedExpressionPersistenceAdapter — CRUD cho named_expression table
```

---

## 6. Promote-or-reuse logic (application layer)

`ExpressionTreeService.persist()` xử lý khi lưu một `ExpressionNode`:

```
case Inline(name, spel):
  // Inline KHÔNG bao giờ được persist trực tiếp.
  // App layer promote lên NamedExpression trước, sau đó persist dưới dạng LibraryRef.
  existing = NamedExpressionRepository.findBySpel(spel)
  if existing.present:
    → dùng existing.id → persist LibraryRef(existing.id, parentId)
  else:
    // name là bắt buộc tại đây (validated ở presentation layer trước khi vào)
    named = NamedExpression.create(name, spel)
    saved = NamedExpressionRepository.save(named)
    → persist LibraryRef(saved.id, parentId)

case LibraryRef(refId):
  → INSERT abac_expression(type=LITERAL, named_expression_id=refId.value, parent_id=...)
  → không dedup (mỗi rule/policy có row riêng nhưng cùng trỏ vào named_expression)

case Composition(op, children):
  → INSERT abac_expression(type=COMPOSITION, combination_type=op)
  → persist từng child với parent_id = this row id
```

> **Hệ quả**: Sau khi persist, tree trong DB chỉ chứa `LibraryRef` (LITERAL với named_expression_id) và `Composition` — không có `Inline` row nào.

---

## 7. Delete behavior

Vì Inline không còn tồn tại trong DB, logic xóa đơn giản hơn:

```
deleteTree(rootDbId):
  row = load abac_expression by id
  if LITERAL (luôn là LibraryRef):
    → DELETE row này (row riêng của rule/policy, không phải NamedExpression AR)
    → KHÔNG xóa named_expression
  if COMPOSITION:
    children.forEach(child → deleteTree(child.id))
    → DELETE row này
```

Xóa `NamedExpression` AR (qua `DeleteNamedExpression` use case):
```
isInUse = đếm abac_expression rows có named_expression_id = this id
         + đếm policy/rule rows có target/condition_expression_id trỏ đến inline named
if inUse → throw NAMED_EXPRESSION_IN_USE
else → DELETE named_expression row
```

---

## 8. Application service: `ExpressionTreeService`

```java
@Service
public class ExpressionTreeService {

    // Persist cây ExpressionNode, trả về DB row id của root node (lưu vào FK của rule/policy)
    // Inline node được promote → NamedExpression + LibraryRef tại đây (không bao giờ persist Inline trực tiếp)
    Long persist(ExpressionNode node, Long parentId);

    // Load cây từ DB root id → ExpressionNode tree (dùng bởi PolicyMapper khi reconstruct domain object)
    ExpressionNode loadTree(Long rootId);

    // Resolve DB root id → flat SpEL string (convenience method dùng bởi AdminPolicyProvider)
    String resolveSpel(Long rootId);

    // Resolve ExpressionNode tree → flat SpEL string (expand LibraryRef qua NamedExpressionRepository)
    String resolveFromNode(ExpressionNode node);

    // Xóa expression tree, không xóa NamedExpression AR
    void deleteTree(Long rootId);
}
```

`AdminPolicyProvider` gọi `resolveSpel(rule.getConditionExpressionId())` thay vì đọc thẳng string.

`resolveSpel` → `resolveFromNode` → expand `LibraryRef` qua `NamedExpressionRepository.findById()`.

---

## 9. API contract

### Request — `ExpressionNodeRequest` (recursive)

```json
{ "type": "COMPOSITION", "operator": "AND", "children": [
    { "type": "LIBRARY_REF", "refId": 5 },
    { "type": "INLINE", "name": "Cùng tenant",
      "spel": "subject.getAttribute('tenantId') == object.data.tenantId" },
    { "type": "INLINE", "spel": "action.getAttribute('name') == 'DELETE'" }
]}
```

### Response — `ExpressionNodeView`

```json
{ "type": "COMPOSITION", "operator": "AND",
  "resolvedSpel": "(subject.roles.contains('ADMIN') && action.name == 'DELETE')",
  "children": [
    { "type": "LIBRARY_REF", "refId": 5, "name": "Là admin",
      "resolvedSpel": "subject.roles.contains('ADMIN')" },
    { "type": "INLINE", "name": null,
      "resolvedSpel": "action.name == 'DELETE'" }
]}
```

`resolvedSpel` luôn có ở mọi node — FE dùng để hiển thị preview và detect builder mode.

### Endpoints mới

```
GET    /api/admin/v1/abac/expressions          → list NamedExpressions (dropdown source)
DELETE /api/admin/v1/abac/expressions/{id}     → delete với usage check
```

---

## 10. Frontend — modes per node

### Creation / Edit input: 3 modes

Khi nhập liệu (tạo mới hoặc chủ động sửa), user thấy 3 mode:

**Ref mode** — select từ NamedExpression library:
- Dropdown: `name + preview SpEL`
- Read-only SpEL preview khi đã chọn
- → persist: `LibraryRef(selectedId)`

**Builder mode** — visual, single condition:
- Name field (**bắt buộc** — sẽ dùng làm tên NamedExpression mới nếu SpEL chưa tồn tại)
- Condition type dropdown + value fields
- Button "Wrap in AND block" / "Wrap in OR block" → tạo Composition bao quanh
- SpEL preview
- → persist: promote Inline → NamedExpression (find-or-create) → `LibraryRef`

**Raw mode** — SpEL thuần, fallback cho expression phức tạp:
- Name field (**bắt buộc**)
- Textarea SpEL
- SpEL preview
- → persist: giống Builder mode, promote → `LibraryRef`

> **Deduplicate check** (Builder + Raw): trước khi save, check `findBySpel(spel)`. Nếu trùng → reuse `NamedExpression` hiện có, không tạo mới.

### Layout của node editor

```
┌─ [Wrap AND] [Wrap OR] ──────── [Ref | Builder | Raw] ┐
│  ... nội dung tùy mode ...                           │
└──────────────────────────────────────────────────────┘
```

- **Top-left**: Wrap buttons — bọc node hiện tại vào Composition mới. Chỉ hoạt động ở root level của widget; sau khi wrap, component re-render thành Composition block với node cũ là child đầu tiên.
- **Top-right**: Mode switch — chuyển giữa Ref / Builder / Raw cho node đó.
- **Mỗi child node trong Composition** cũng có layout tương tự — mode switch và wrap buttons độc lập.

### Load / View: 2 modes (auto-detect)

FE detect mode khi mở expression đã lưu từ DB — không cần user chọn:

```
LibraryRef   → land vào Ref mode (dropdown pre-selected với name đã đặt)
Composition  → land vào Builder mode (composite block, hiển thị children)
```

`Inline` không tồn tại trong DB nên không có case load → Raw mode.

Raw mode chỉ xuất hiện khi user chủ động switch từ Builder sang Raw để sửa tay.

> **Lưu ý UX**: Sau khi save từ Builder hoặc Raw mode, khi load lại user sẽ thấy **Ref mode** với name đã đặt pre-selected trong dropdown — vì expression đã được promote thành `NamedExpression + LibraryRef`. Để sửa SpEL, user phải vào trang Expression Library (`/admin/abac/expressions`).

### COMPOSITION node display

```
┌─ AND Block ─── [Wrap AND] [Wrap OR] ─ [Ref|Builder|Raw] ─ [×] ┐
│                                                                 │
│  ┌─ child 1 ─── [Wrap AND] [Wrap OR] ─ [Ref|Builder|Raw] ─┐   │
│  │  📌 Là admin  (Ref mode — dropdown pre-selected)        │   │
│  └──────────────────────────────────────────────────────── ┘   │
│                                                                 │
│  ┌─ child 2 ─── [Wrap AND] [Wrap OR] ─ [Ref|Builder|Raw] ─┐   │
│  │  Name: "Cùng tenant"  SpEL: ...  (Builder mode)         │   │
│  └──────────────────────────────────────────────────────── ┘   │
│                                                                 │
│  [+ Add node]                                                   │
└─────────────────────────────────────────────────────────────── ┘
```

---

## 11. Implementation plan

### Phase 1 — DB + Domain
1. `V8__named_expression_and_tree_ref.sql` migration
2. Tạo `domain/abac/expression/`: `NamedExpression`, `NamedExpressionId`, `NamedExpressionRepository`
3. Định nghĩa `ExpressionNode` sealed interface trong `domain/abac/policy/`
4. Xóa `ExpressionVO`, update `RuleDefinition`, `PolicyDefinition`
5. Infrastructure: `NamedExpressionJpaEntity`, `NamedExpressionJpaRepository`, `NamedExpressionPersistenceAdapter`
6. Update `AbacExpressionJpaEntity` + `AbacExpressionJpaRepository`

### Phase 2 — Application layer
1. Implement `ExpressionTreeService`
2. Update `CreateRule`, `UpdateRule`, `DeleteRule`
3. Update `UpdatePolicy`, `DeletePolicy`
4. Update `PolicyMapper` — load tree, reconstruct `ExpressionNode`
5. Update `AdminPolicyProvider` — dùng `resolveSpel()`
6. Thêm `ListNamedExpressions`, `DeleteNamedExpression` use cases

### Phase 3 — Presentation layer
1. `ExpressionNodeRequest` / `ExpressionNodeView` models
2. Update `CreateRuleRequest`, `UpdateRuleRequest`, `UpdatePolicyRequest`
3. `ExpressionController` (list named, delete named)
4. Update response assembly cho Rule + Policy views

### Phase 4 — Frontend
1. Thêm expression types vào `policy.service.ts`, tạo `expression.service.ts`
2. Rewrite `RuleExpressionBuilderComponent` — 3 modes, recursive node editor
3. Implement SpEL reverse parser (`spelParser.ts`)
4. Update `CreateRuleDialogComponent`, `EditRuleDialogComponent`
5. Thêm Expression Library page (`/admin/abac/expressions`)

---

## 12. Những gì KHÔNG thay đổi

- `ExpressionType.LITERAL | COMPOSITION` enum trong `abac_expression` — giữ nguyên
- API path của Rule, Policy — không thay đổi prefix
- `SpelValidator` — giữ nguyên, vẫn validate resolvedSpel trước khi persist
- `SpelExpressionAnalyzer` (Rule Impact Preview) — nhận resolved SpEL string, không biết về tree
- Audit log schema — không thay đổi

---

*Tài liệu thiết kế — 2026-04-14*
