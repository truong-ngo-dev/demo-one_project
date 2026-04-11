# Prompt: Batch 1 — Resource & Action Catalogue (UC-019)

**Vai trò**: Bạn là Senior Backend Engineer thực hiện Batch 1 của ABAC Policy Management trong `services/admin`. Đây là bước đặt nền tảng — code phải clean, đúng convention, vì Batch 2 và 3 sẽ build tiếp trên đây.

## Tài liệu căn cứ

1. Task plan + DB schema: @tasks/012_admin_phase1_abac_console.md (Task 1, 2, 3, 4, 5 — scope resource)
2. Use case spec: @services/admin/docs/use-cases/UC-019_resource_action_catalogue.md
3. Convention bắt buộc: @docs/conventions/ddd-structure.md
4. Tài liệu: @services/admin/SERVICE_MAP.md, @services/admin/CLAUDE.md, @CLAUDE.md

## Nhiệm vụ cụ thể

1. **Flyway Migration** — tạo `V3__abac_schema.sql` với toàn bộ 7 bảng ABAC (schema chi tiết trong task plan Task 1). Tất cả bảng trong 1 file duy nhất.

2. **Domain Layer** — tạo `ResourceDefinition` (Aggregate Root) + `ActionDefinition` (Entity) + `ResourceId`, `ActionId` + `ResourceDefinitionRepository` port + `AbacErrorCode`, `AbacException`. Theo spec Task 2 trong task plan. Pattern tham chiếu: `domain/role/Role.java`.

3. **Application Layer** — tạo 8 slices trong `application/resource/` (6 command + 2 query). Spec handler logic xem Task 3 trong task plan. Pattern tham chiếu: `application/role/create/CreateRole.java`.

4. **Infrastructure Layer** — `ResourceDefinitionJpaEntity`, `ActionDefinitionJpaEntity`, `ResourceDefinitionMapper`, `ResourceDefinitionJpaRepository`, `ResourceDefinitionPersistenceAdapter`. Spec xem Task 4 trong task plan. Lưu ý: `existsByIdWithPolicyRef` và `existsByIdWithUIElementRef` để `return false` với comment `// TODO: implement Batch 2/3`.

5. **Presentation Layer** — `ResourceDefinitionController` tại `/api/v1/abac/resources` với 8 endpoints. Spec xem Task 5 trong task plan.

**Không implement**: PolicySet, Policy, Rule, UIElement, Simulate.

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong code và `mvn clean compile -DskipTests` thành công, cung cấp 2 block:

### BATCH 2 CONTEXT BLOCK
Liệt kê: files đã tạo theo layer, package paths thực tế, pattern decisions (nếu khác spec), TODO list còn lại.

### FRONTEND CONTEXT BLOCK
Cung cấp đủ để Angular dev implement mà không cần đọc lại Java code:
- Base URL qua Web Gateway (prefix `/api/admin`)
- TypeScript interfaces cho tất cả request/response types
- Danh sách endpoints với method, path, request body, response shape
- Error codes + message gợi ý cho UI
- UI notes: field nào immutable, route đề xuất, sidebar entry cần thêm
