# Phân tích UX Convenience: ABAC Authorization System

"Thuận tiện" có nghĩa khác nhau hoàn toàn với từng nhóm người dùng hệ thống.
Tài liệu này phân tích từng nhóm, điểm đau thực tế, và cách thiết kế để giải quyết.

---

## Nhóm 1: End User (người dùng dashboard)

### Điểm đau
- **Dead end**: User click vào menu, nhận 403 → không hiểu tại sao, không biết làm gì tiếp.
- **Flickering UI**: FE render xong rồi mới ẩn button/tab sau khi AuthzContext load → giao diện giật.
- **Context switching chậm**: Mỗi lần load trang phải đợi AuthzContext → ảnh hưởng perceived performance.
- **Mất định hướng**: User biết mình "trước kia làm được", giờ không thấy nút → không biết role mình thay đổi hay bug.

### Giải pháp

**1. Load AuthzContext trước khi render bất kỳ UI nào**

```
Login thành công
    → Load AuthzContext (1 API call)
    → Lưu vào memory (Signal/Store)
    → Mới bắt đầu render route đầu tiên
```

Không render page rồi mới ẩn — ẩn ngay từ đầu, không flicker.

**2. Graceful degradation thay vì hard block**

| Tình huống                                           | Cách xử lý tệ                               | Cách xử lý tốt                                     |
|------------------------------------------------------|---------------------------------------------|----------------------------------------------------|
| Tab không có quyền                                   | Hiện tab, click → 403                       | Ẩn tab hoàn toàn                                   |
| Button không có quyền                                | Hiện nút disabled + tooltip "No permission" | Ẩn nút                                             |
| Route không có quyền                                 | Render trang rồi hiện error                 | Guard redirect về trang mặc định trước khi render  |
| Action không có quyền ở instance level (data cụ thể) | 403 response lạnh                           | Toast: "Bạn không có quyền thực hiện thao tác này" |

**3. Không bao giờ nói "Access Denied" với end user**

Thay bằng: không hiện, hoặc nếu phải giải thích thì dùng ngôn ngữ nghiệp vụ:
- ✗ "403 Forbidden"
- ✓ "Tính năng này không có trong gói dịch vụ của bạn"
- ✓ "Liên hệ quản trị viên để được cấp quyền"

**4. AuthzContext cache với version**

Sau khi load lần đầu, lưu vào memory. Refresh chỉ khi:
- User logout / re-login
- Server push event: "policy của bạn vừa thay đổi" (WebSocket)

Không bao giờ poll theo interval.

---

> **Admin UX**: Xem thiết kế chi tiết giao diện quản trị tại [`abac_admin_console_design.md`](abac_admin_console_design.md) — bao gồm Visual Policy Builder, Simulator, Impact Analysis, Audit Log, Reverse/Forward Lookup.

---

## Nhóm 2: Developer (người tích hợp UIElement vào code)

### Điểm đau
- Phải đăng ký UIElement thủ công vào DB/JSON → hay quên, hay không đồng bộ với code
- ElementId trong template FE và trong registry BE phải khớp → typo là bug không có error message
- Thêm feature mới → phải làm 3 chỗ: viết UI, đăng ký UIElement, viết Policy → dễ bỏ sót

### Giải pháp: Code-first UIElement Declaration

Thay vì đăng ký UIElement qua database admin, developer khai báo ngay trong code:

**BE — Annotation trên controller method:**
```java
@GetMapping("/users/{id}")
@UiElement(
    id = "tab:user-detail:profile",
    type = ElementType.TAB,
    resourceRef = "user",
    actionRef = "READ",
    label = "Profile",
    group = "user-detail-tabs",
    order = 0
)
@PreEnforce
public ResponseEntity<UserDetail> getUserById(@PathVariable String id) { ... }
```

Khi service khởi động, annotation scanner tự đăng ký UIElement vào registry.
Tương tự cách `@RequestMapping` tự đăng ký route.

**FE — Generated constants (không hardcode string):**

Thay vì:
```typescript
this.authz.permitted('btn:user:lock-toggle')  // Typo không có compile error
```

Generate một file constants từ registry API:
```typescript
// generated/ui-element-ids.ts — auto-generated, không sửa tay
export const UI = {
  USER_DETAIL: {
    TAB_PROFILE:        'tab:user-detail:profile',
    BTN_LOCK_TOGGLE:    'btn:user:lock-toggle',
    BTN_ASSIGN_ROLE:    'btn:user:assign-role',
    BTN_REMOVE_ROLE:    'btn:user:remove-role',
    TAB_SECURITY:       'tab:user-detail:security',
    BTN_SESSION_REVOKE: 'btn:user-session:revoke',
  }
} as const;

// Dùng trong component:
canLockUser = this.authz.permitted(UI.USER_DETAIL.BTN_LOCK_TOGGLE);
//                                 ^^^ TypeScript autocomplete + compile error nếu sai
```

**Safe default khi UIElement mới chưa có policy:**

```
UIElement đăng ký nhưng chưa có policy → DENY by default (an toàn)
                                        + Admin nhận notification:
                                          "3 UIElement mới chưa có policy, cần cấu hình"
```

---

## Tổng hợp: Responsibility theo nhóm

| Tính năng | End User | Developer |
|---|:---:|:---:|
| AuthzContext load trước render (no flicker) | ✓ | |
| Graceful hide (không nói "403") | ✓ | |
| WebSocket refresh khi policy thay đổi | ✓ | |
| Code-first UIElement declaration | | ✓ |
| Generated TypeScript constants | | ✓ |
| Auto-notify khi UIElement chưa có policy | | ✓ |

---

## Điểm quan trọng nhất

Toàn bộ ABAC engine tốt đến đâu cũng vô nghĩa nếu Admin không có tool để **hiểu và kiểm soát** nó.

Hệ thống cần **hai lớp UX song song**:
- **Runtime UX** (end user): personalized, seamless, no friction
- **Management UX** (admin): transparent, predictable, reversible

Thiếu lớp thứ hai, ABAC sẽ trở thành một black box mà sau 6 tháng không ai dám đụng vào vì "không biết thay đổi gì sẽ break gì".
