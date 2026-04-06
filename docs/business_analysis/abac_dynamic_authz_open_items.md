# Open Items: ABAC with Dynamic Navigation Based on Authorization

> Những phần chưa được bàn hoặc chưa chốt design.
> Cần giải quyết trước hoặc trong quá trình implement.

---

## 1. Resource & Action Catalogue

**Vấn đề chưa giải quyết:**
- Ai định nghĩa danh sách resource hợp lệ? (employee, user, maintenance_request...)
- Ai định nghĩa danh sách action hợp lệ per resource? (LIST, READ, CREATE, UPDATE, DELETE, LOCK, APPROVE...)
- Lưu ở đâu? Hardcode enum hay persistent trong DB?
- Validation: policy reference resource không tồn tại → lỗi ngay hay runtime fail?

**Câu hỏi cần chốt:**
- Catalogue là static (code) hay dynamic (DB, admin có thể thêm)?
- Có cần versioning cho catalogue không?

> Note: Không có PLATFORM-level catalogue — BQL admin là đơn vị định nghĩa resource/action trong tòa của mình.

---

## 2. Resource Type Registry

**Vấn đề chưa giải quyết:**
- BE nhận `{ resource: "employee", data: {...} }` từ FE dạng `Map<String, Object>`
- Policy SpEL dùng `object.data.department` (dot notation) cần typed object
- Cần mapping: resource name → Java class để deserialize

**Câu hỏi cần chốt:**
- Registry khai báo bằng annotation hay config?
- Xử lý thế nào khi FE gửi data không đủ field mà policy cần?

---

## 3. UIElement Registry Implementation

**Vấn đề chưa giải quyết:**
- UIElement được lưu ở đâu? DB hay code?
- Developer đăng ký UIElement bằng cách nào? (annotation, config file, admin UI?)
- Đồng bộ với GRANTS constants ở FE như thế nào? (manual hay generate?)
- Lifecycle: UIElement bị xóa thì policy liên quan xử lý thế nào?

---

## 4. Admin UI Concerns

Những tính năng cần thiết cho admin vận hành ABAC:

**Must have:**
- CRUD cho Policy/Rule (thay thế JSON file)
- Visual Policy Builder (form-based, không cần viết SpEL thủ công)
- Resource & Action catalogue browser

**Should have:**
- Permission Simulator: chọn user → xem họ được thấy gì
- Traceability: tại sao element X bị DENY (rule nào, condition nào fail)
- Reverse lookup: element X → policy nào control → user nào bị ảnh hưởng

**Nice to have:**
- Impact Analysis: thay đổi policy → bao nhiêu user bị ảnh hưởng
- Audit log: ai thay đổi policy gì, khi nào

---

## 5. Subject Enrichment Chi tiết

**Vấn đề chưa giải quyết:**
- SubjectProvider enrich những gì cụ thể cho từng role?
- Khi nào enrich là bắt buộc vs optional?
- Cache strategy cho enriched subject (TTL? invalidation khi role thay đổi?)
- Party service chưa tồn tại — SubjectProvider làm gì tạm thời?

---

## 6. Policy Storage & Migration

**Vấn đề chưa giải quyết:**
- DB schema cụ thể cho policy_set, policy, rule, expression
- Migration từ JSON file (demo) sang DB
- Policy versioning: khi update policy thì cache ở services khác invalidate thế nào?

---

---

## 8. Navigation-level Grants — Scope cụ thể

**Vấn đề chưa giải quyết:**
- `POST /authorization-context { scope: "navigation" }` trả về những grants nào?
- BE biết navigation scope bao gồm resource/action nào?
- Có cần filter theo service không? (user chỉ cần grants của service đang dùng)

---

## 8. Tenant ABAC Layer

Đã thống nhất làm sau, độc lập. Khi làm cần giải quyết:
- PAP scoping: tenant admin chỉ viết policy trong scope của mình
- Two-phase evaluation implementation cụ thể
- Delegated authorization UX cho tenant admin

---

## 9. Cache & Performance

**Chưa bàn:**
- AuthzContext cache ở FE: memory only, không persist — đã rõ
- BE-side cache cho policy: chưa design
- Instance-level grants cache: có cần không? (FE gọi mỗi lần vào detail page)
- Policy change notification: WebSocket/SSE để FE refresh AuthzContext

---

## 10. Integration với Existing Admin Service

**Chưa xem xét:**
- ABAC domain model đặt ở package nào trong admin service?
- Conflict với domain User/Role hiện có không?
- SecurityConfiguration của admin service cần update gì?
- DB migration strategy (thêm tables mới không ảnh hưởng existing schema)
