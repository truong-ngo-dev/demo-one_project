# Frontend Implementation — ExpressionNode Name Required + Layout

*2026-04-15*

---

## Bối cảnh

Backend đã thay đổi: INLINE expression bắt buộc phải có `name` để promote thành `NamedExpression` trước khi persist. Frontend cần enforce constraint này và cập nhật layout header của node editor theo thiết kế mới:

```
┌─ [Wrap AND] [Wrap OR] ──────── [Builder | Library | Raw] ┐
│  ... nội dung tùy mode ...                               │
└──────────────────────────────────────────────────────────┘
```

---

## Task 1 — Layout: gộp wrap buttons và tab switch vào cùng một header row

**File:** `web/src/app/dashboard/abac/policies/policy-detail/rule-expression-builder/rule-expression-builder.html`

**Tìm đoạn tab switcher ở đầu file:**

```html
  <!-- Tab switcher -->
  <div class="builder-mode-header">
    <div class="mode-pill-group">
      <button type="button" class="mode-pill-btn" [class.active]="activeTab() === 'builder'"
              (click)="switchTab('builder')">Builder</button>
      <button type="button" class="mode-pill-btn" [class.active]="activeTab() === 'library'"
              (click)="switchTab('library')">Library</button>
      <button type="button" class="mode-pill-btn" [class.active]="activeTab() === 'raw'"
              (click)="switchTab('raw')">Raw</button>
    </div>
  </div>
```

**Thay bằng** (wrap buttons bên trái, mode pills bên phải):

```html
  <!-- Header: wrap actions (left) + mode switch (right) -->
  <div class="builder-mode-header">
    @if (depth < 3) {
      <div class="wrap-actions">
        <button type="button" mat-stroked-button class="wrap-btn wrap-and"
                (click)="wrapInBlock('AND')">
          Wrap AND
        </button>
        <button type="button" mat-stroked-button class="wrap-btn wrap-or"
                (click)="wrapInBlock('OR')">
          Wrap OR
        </button>
      </div>
    }
    <div class="mode-pill-group">
      <button type="button" class="mode-pill-btn" [class.active]="activeTab() === 'builder'"
              (click)="switchTab('builder')">Builder</button>
      <button type="button" class="mode-pill-btn" [class.active]="activeTab() === 'library'"
              (click)="switchTab('library')">Library</button>
      <button type="button" class="mode-pill-btn" [class.active]="activeTab() === 'raw'"
              (click)="switchTab('raw')">Raw</button>
    </div>
  </div>
```

**Tìm và xóa** đoạn wrap buttons ở cuối file (không còn cần nữa):

```html
  <!-- ── WRAP BUTTONS ────────────────────────────────────────────────────── -->
  @if (depth < 3) {
    <div class="wrap-actions">
      <button type="button" mat-stroked-button class="wrap-btn wrap-and"
              (click)="wrapInBlock('AND')">
        Wrap AND
      </button>
      <button type="button" mat-stroked-button class="wrap-btn wrap-or"
              (click)="wrapInBlock('OR')">
        Wrap OR
      </button>
    </div>
  }
```

---

## Task 2 — Thêm name field vào Builder tab + make name required ở Raw tab

**File:** `web/src/app/dashboard/abac/policies/policy-detail/rule-expression-builder/rule-expression-builder.html`

### 2a — Builder tab: thêm name field ở đầu

Tìm dòng mở đầu builder tab:

```html
  @if (activeTab() === 'builder') {
    <div class="condition-list">
```

Thêm name field vào **trước** `<div class="condition-list">`:

```html
  @if (activeTab() === 'builder') {
    <mat-form-field appearance="outline" class="raw-name-field" floatLabel="always">
      <mat-label>Name *</mat-label>
      <input matInput placeholder="e.g. Là admin, Cùng tenant, Cùng phòng ban"
             [value]="rawName()"
             (input)="onRawNameInput($any($event.target).value)" />
      <mat-hint>Tên để tái sử dụng trong Expression Library</mat-hint>
    </mat-form-field>
    <div class="condition-list">
```

### 2b — Raw tab: đổi label từ optional thành required

Tìm:

```html
      <mat-form-field appearance="outline" class="raw-name-field" floatLabel="always">
        <mat-label>Name (optional)</mat-label>
        <input matInput
               placeholder="Name (optional) — displayed instead of SpEL in the tree"
               [value]="rawName()"
               (input)="onRawNameInput($any($event.target).value)" />
      </mat-form-field>
```

Thay bằng:

```html
      <mat-form-field appearance="outline" class="raw-name-field" floatLabel="always">
        <mat-label>Name *</mat-label>
        <input matInput
               placeholder="e.g. Là admin, Cùng tenant — sẽ lưu vào Expression Library"
               [value]="rawName()"
               (input)="onRawNameInput($any($event.target).value)" />
        <mat-hint>Bắt buộc — tên để tái sử dụng trong Expression Library</mat-hint>
      </mat-form-field>
```

---

## Task 3 — Enforce name required trước khi emit

**File:** `web/src/app/dashboard/abac/policies/policy-detail/rule-expression-builder/rule-expression-builder.ts`

Tìm method `buildCurrentNode()`:

```text
  private buildCurrentNode(): ExpressionNodeRequest | null {
    switch (this.activeTab()) {
      case 'builder': {
        const spel = this.builderSpel();
        if (!spel) return null;
        return { type: 'INLINE', spel, name: this.rawName() || undefined };
      }
      case 'library': {
        const refId = this.selectedRefId();
        if (refId === null) return null;
        return { type: 'LIBRARY_REF', refId };
      }
      case 'raw': {
        const spel = this.rawSpel().trim();
        if (!spel) return null;
        return { type: 'INLINE', spel, name: this.rawName() || undefined };
      }
    }
  }
```

Thay bằng:

```text
  private buildCurrentNode(): ExpressionNodeRequest | null {
    switch (this.activeTab()) {
      case 'builder': {
        const spel = this.builderSpel();
        if (!spel) return null;
        const name = this.rawName().trim();
        if (!name) return null;   // name required — backend rejects nameless INLINE
        return { type: 'INLINE', spel, name };
      }
      case 'library': {
        const refId = this.selectedRefId();
        if (refId === null) return null;
        return { type: 'LIBRARY_REF', refId };
      }
      case 'raw': {
        const spel = this.rawSpel().trim();
        if (!spel) return null;
        const name = this.rawName().trim();
        if (!name) return null;   // name required — backend rejects nameless INLINE
        return { type: 'INLINE', spel, name };
      }
    }
  }
```

---

## Tóm tắt file bị thay đổi

| File                           | Thay đổi                                                |
|--------------------------------|---------------------------------------------------------|
| `rule-expression-builder.html` | Layout: wrap buttons lên top-left, mode pills top-right |
| `rule-expression-builder.html` | Builder tab: thêm name field (required)                 |
| `rule-expression-builder.html` | Raw tab: name từ optional → required                    |
| `rule-expression-builder.ts`   | `buildCurrentNode()`: return null nếu name blank        |

**Không sửa:** `expression-node-editor.ts`, `expression-node-editor.html`, `policy.service.ts`, `expression.service.ts`, routes — đã đúng.

---

## Ghi chú kỹ thuật

- `rawName` signal được dùng chung cho cả Builder tab và Raw tab — khi user switch giữa 2 tab, name được giữ nguyên. Đây là intentional.
- `buildCurrentNode()` trả về `null` khi name còn trống → form được coi là incomplete → nút Save sẽ disabled (vì expression là null).
- Sau khi save từ Builder hoặc Raw mode và load lại: component sẽ hiển thị **Library tab** với tên đã đặt pre-selected trong dropdown (vì backend đã promote thành `LibraryRef`). Đây là behavior mong đợi.

## CSS — cần cập nhật sau Task 1

**File:** `web/src/app/dashboard/abac/policies/policy-detail/rule-expression-builder/rule-expression-builder.css`

Sau khi Task 1 gộp wrap buttons và mode pills vào cùng `builder-mode-header`, class này cần thành flex row với hai nhóm ở hai đầu. Gemini tự xử lý giá trị cụ thể, nhưng yêu cầu kết quả:

- `builder-mode-header`: flex row, `align-items: center`, `justify-content: space-between`
- `wrap-actions`: flex row, gap nhỏ, nằm bên trái
- `mode-pill-group`: giữ nguyên style hiện tại, nằm bên phải
