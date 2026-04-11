Prompt: Phase 2 Batch 2 (BE) — Rule Impact Preview (UC-031)

Vai trò: Bạn là Senior Backend Engineer thực hiện Phase 2 Batch 2 của ABAC Admin Console.
Phase 2 Batch 1 đã xong (Navigation Simulator — UC-030).
Batch 2 implement Impact Analysis: phân tích subject-side conditions của một rule trước khi admin lưu.

Tài liệu căn cứ:
  1. Design: @docs/business_analysis/abac_admin_console_design.md (Section 7 — Impact Analysis)
  2. Rule domain: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/domain/abac/policy/RuleDefinition.java
  3. Convention: @docs/conventions/ddd-structure.md
  4. Service map: @services/admin/SERVICE_MAP.md

Context từ Phase 1:
  - SpEL validation đã có: `SpelValidator.validate(spel)` (dùng SpelExpressionParser)
  - RuleDefinition: `targetExpression`, `conditionExpression` là `ExpressionVO` (chứa `spelExpression` string)
  - Rule update endpoint: `PUT /api/v1/abac/policies/{policyId}/rules/{ruleId}` (UpdateRuleRequest)

Mục đích của Impact Preview:
  Trước khi admin lưu thay đổi targetExpression / conditionExpression của một rule, hệ thống phân tích
  các biểu thức và trả về:
  - Các roles bắt buộc (trích từ `subject.roles.contains('X')`)
  - Các subject attributes cần thiết (trích từ `subject.getAttribute('X')...`)
  - Actions cụ thể mà rule áp dụng (trích từ `action.getAttribute('name') == 'X'`)
  - Liệu rule có áp dụng cho navigation level không (object.data == null → navigable)
  - Cảnh báo nếu expression quá phức tạp để parse

  Phase 2 scope: chỉ phân tích subject-side + action conditions ở mức literal pattern matching.
  Không phân tích được số user bị ảnh hưởng (phụ thuộc instance data).

Nhiệm vụ cụ thể:

  1. UC Doc
      - Tạo `services/admin/docs/use-cases/UC-031_rule_impact_preview.md`

  2. Application Layer — `application/rule/query/impact_preview/GetRuleImpactPreview.java`
      - Query:
        ```java
        public record Query(String targetExpression, String conditionExpression) {}
        ```
      - Result:
        ```java
        public record Result(
            List<String> requiredRoles,           // trích từ subject.roles.contains('X')
            List<String> requiredAttributes,      // trích từ subject.getAttribute('X')
            List<String> specificActions,         // trích từ action.getAttribute('name') == 'X'
            boolean navigableWithoutData,         // true nếu có object.data == null branch
            boolean hasInstanceCondition,         // true nếu có object.data != null condition
            String parseWarning                   // null nếu parse OK, message nếu quá phức tạp
        ) {}
        ```
      - Handler logic — dùng Spring SpEL `SpelExpressionParser` + AST walking:
        1. Parse `targetExpression` và `conditionExpression` (bỏ qua null)
        2. AST walker tìm các pattern sau (dùng `SpelNode` visitor):
           - `MethodCallExpression("contains")` trên `PropertyOrFieldAccessor("roles")` trên `PropertyOrFieldAccessor("subject")`
             + argument là `StringLiteral` → thêm vào `requiredRoles`
           - `MethodCallExpression("getAttribute")` trên `PropertyOrFieldAccessor("subject")`
             + argument là `StringLiteral` → thêm vào `requiredAttributes`
           - `OpEQ` với left = `MethodCallExpression("getAttribute")` trên `PropertyOrFieldAccessor("action")`
             + argument "name" và right là `StringLiteral` → thêm vào `specificActions`
           - `OpEQ` với `PropertyOrFieldAccessor("data")` trên `PropertyOrFieldAccessor("object")` và right = `NullLiteral`
             → `navigableWithoutData = true`
           - Bất kỳ access nào vào `object.data` không phải null check → `hasInstanceCondition = true`
        3. Nếu parse throw exception → `parseWarning = "Expression could not be parsed: " + message`
           Trả Result với tất cả lists rỗng + parseWarning set
        4. Nếu parse OK nhưng walker gặp lỗi → `parseWarning = "Expression too complex for analysis"`
      - Pattern: implement SpelNode walker trong private method/inner class, không cần Spring bean riêng
      - Không inject repository (stateless query — chỉ phân tích chuỗi đầu vào)

  3. Presentation Layer — thêm vào `PolicyController.java` hoặc tạo `RuleImpactController.java`
      - Endpoint: `POST /api/v1/abac/rules/impact-preview`
      - Request body:
        ```java
        record ImpactPreviewRequest(String targetExpression, String conditionExpression) {}
        ```
        Cả hai field đều optional (null → bỏ qua trong analysis)
      - Response: `ApiResponse<GetRuleImpactPreview.Result>`
      - Không cần auth vì đây là analysis-only, không có side effect

  4. UC Index — cập nhật `UC-000_index.md` thêm UC-031

Không implement: Đếm số user bị ảnh hưởng (Phase 3 — cần query DB users by role/attribute),
reverse lookup (Phase 3).

Yêu cầu Handoff (Bắt buộc):
  Sau khi xong và `mvn clean compile -DskipTests` thành công, cung cấp:

  PHASE 2 BATCH 2 CONTEXT BLOCK
    - Package path của GetRuleImpactPreview.Handler
    - Danh sách patterns mà walker detect được (để FE hiểu limitations)
    - Known limitations: chỉ phát hiện literal pattern, không detect dynamic expression

  FRONTEND CONTEXT BLOCK — Phase 2 Batch 2
    TypeScript interfaces:
      ImpactPreviewRequest { targetExpression?: string; conditionExpression?: string }
      ImpactPreviewResult {
        requiredRoles: string[];
        requiredAttributes: string[];
        specificActions: string[];
        navigableWithoutData: boolean;
        hasInstanceCondition: boolean;
        parseWarning: string | null;
      }

    Endpoint: POST /api/admin/v1/abac/rules/impact-preview → ApiResponse<ImpactPreviewResult>

    UI notes:
      - parseWarning != null → hiển thị warning banner, vẫn cho phép confirm và save
      - requiredRoles/Attributes empty + specificActions empty → rule áp dụng cho tất cả (universal rule)
      - navigableWithoutData = true → rule PERMIT mà không cần data cụ thể (navigation level)
      - hasInstanceCondition = true → rule chỉ đánh giá chính xác khi có instance data
