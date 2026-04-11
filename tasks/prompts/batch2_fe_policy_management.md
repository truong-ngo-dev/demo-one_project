Prompt: Batch 2 (FE) — PolicySet / Policy / Rule Management

Vai trò: Bạn là Senior Frontend Engineer chuyên về Angular và kiến trúc Dashboard Admin.

Tài liệu căn cứ:
  1. Quy ước dự án: @web/CLAUDE.md
  2. Cấu trúc routes hiện tại: @web/src/app/app.routes.ts
  3. Pattern tham chiếu service: @web/src/app/core/services/resource.service.ts
  4. Pattern tham chiếu component list: @web/src/app/dashboard/abac/resources/resources.ts
  5. Pattern tham chiếu component detail: @web/src/app/dashboard/abac/resources/resource-detail/resource-detail.ts
  6. Sidebar hiện tại: @web/src/app/dashboard/dashboard.html

Dữ liệu từ Backend (Bàn giao từ Batch 2):
Endpoints

PolicySet — /api/v1/abac/policy-sets

┌────────┬───────────────────────────────┬──────────────────┬──────────────────────────────────────┐  
│ Method │             Path              │       Body       │             Description              │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ GET    │ /api/v1/abac/policy-sets      │ —                │ List (params: keyword, page, size) → │  
│        │                               │                  │  PagedApiResponse<PolicySetSummary>  │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ GET    │ /api/v1/abac/policy-sets/{id} │ —                │ Detail + nested policies →           │  
│        │                               │                  │ ApiResponse<PolicySetDetail>         │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ POST   │ /api/v1/abac/policy-sets      │ PolicySetRequest │ Create → ApiResponse<{id}>  (201)    │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ PUT    │ /api/v1/abac/policy-sets/{id} │ PolicySetRequest │ Update → 204                         │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ DELETE │ /api/v1/abac/policy-sets/{id} │ —                │ Delete → 204                         │  
└────────┴───────────────────────────────┴──────────────────┴──────────────────────────────────────┘

Policy — /api/v1/abac/policies

┌────────┬─────────────────────────────────────┬───────────────┬───────────────────────────────────┐  
│ Method │                Path                 │     Body      │            Description            │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ GET    │ /api/v1/abac/policies?policySetId=X │ —             │ List by PolicySet →               │  
│        │                                     │               │ ApiResponse<PolicySummary[]>      │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ GET    │ /api/v1/abac/policies/{id}          │ —             │ Detail + rules →                  │  
│        │                                     │               │ ApiResponse<PolicyDetail>         │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ POST   │ /api/v1/abac/policies               │ PolicyRequest │ Create → ApiResponse<{id}> (201)  │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ PUT    │ /api/v1/abac/policies/{id}          │ PolicyRequest │ Update → 204                      │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ DELETE │ /api/v1/abac/policies/{id}          │ —             │ Delete (cascades rules) → 204     │  
└────────┴─────────────────────────────────────┴───────────────┴───────────────────────────────────┘

Rule — /api/v1/abac/policies/{policyId}/rules

┌────────┬─────────────────────────────────────────────────┬───────────────┬──────────────────────┐   
│ Method │                      Path                       │     Body      │     Description      │   
├────────┼─────────────────────────────────────────────────┼───────────────┼──────────────────────┤   
│        │                                                 │               │ Create →             │   
│ POST   │ /api/v1/abac/policies/{policyId}/rules          │ RuleRequest   │ ApiResponse<{id}>    │   
│        │                                                 │               │ (201)                │   
├────────┼─────────────────────────────────────────────────┼───────────────┼──────────────────────┤   
│ PUT    │ /api/v1/abac/policies/{policyId}/rules/{ruleId} │ RuleRequest   │ Update → 204         │   
├────────┼─────────────────────────────────────────────────┼───────────────┼──────────────────────┤   
│ DELETE │ /api/v1/abac/policies/{policyId}/rules/{ruleId} │ —             │ Delete → 204         │   
├────────┼─────────────────────────────────────────────────┼───────────────┼──────────────────────┤   
│ PUT    │ /api/v1/abac/policies/{policyId}/rules/reorder  │ {ruleIds:     │ Reorder → 204        │   
│        │                                                 │ number[]}     │                      │   
└────────┴─────────────────────────────────────────────────┴───────────────┴──────────────────────┘

TypeScript Interfaces

// PolicySet
interface PolicySetRequest {
name: string;
scope: 'OPERATOR' | 'TENANT';
combineAlgorithm: CombineAlgorithmName;
isRoot: boolean;
tenantId?: string;
}
interface PolicySetSummary {
id: number; name: string; scope: string; combineAlgorithm: string;
isRoot: boolean; tenantId: string | null; createdAt: number; updatedAt: number;
}
interface PolicySetDetail extends PolicySetSummary {
policies: { id: number; name: string; combineAlgorithm: string }[];
}

// Policy
interface PolicyRequest {
policySetId: number;
name: string;
targetExpression?: string;   // SpEL string or null
combineAlgorithm: CombineAlgorithmName;
}
interface PolicySummary {
id: number; name: string; combineAlgorithm: string;
targetExpression: string | null; createdAt: number; updatedAt: number;
}
interface PolicyDetail extends PolicySummary {
policySetId: number;
rules: RuleView[];
}

// Rule
interface RuleRequest {
name: string;
description?: string;
targetExpression?: string;
conditionExpression?: string;
effect: 'PERMIT' | 'DENY';
orderIndex: number;
}
interface RuleView {
id: number; name: string; description: string | null;
targetExpression: string | null; conditionExpression: string | null;
effect: 'PERMIT' | 'DENY'; orderIndex: number;
}

type CombineAlgorithmName =
'DENY_OVERRIDES' | 'PERMIT_OVERRIDES' | 'DENY_UNLESS_PERMIT' |
'PERMIT_UNLESS_DENY' | 'FIRST_APPLICABLE' | 'ONLY_ONE_APPLICABLE';

Error Codes (30007–30011)

┌───────┬──────┬───────────────────────────┐
│ Code  │ HTTP │          Meaning          │
├───────┼──────┼───────────────────────────┤
│ 30007 │ 404  │ Policy Set not found      │
├───────┼──────┼───────────────────────────┤
│ 30008 │ 409  │ Policy Set name duplicate │
├───────┼──────┼───────────────────────────┤
│ 30009 │ 404  │ Policy not found          │
├───────┼──────┼───────────────────────────┤
│ 30010 │ 404  │ Rule not found            │
├───────┼──────┼───────────────────────────┤
│ 30011 │ 400  │ Invalid SpEL expression   │
└───────┴──────┴───────────────────────────┘

UI Notes

- policy.targetExpression is a SpEL string (display in monospace/code textarea)
- rule.conditionExpression is the main authorization expression (SpEL)
- isRoot: true means this PolicySet is the system's active policy (there should be only one)
- combineAlgorithm controls how conflicts between child policies/rules are resolved
- reorder: send full ordered array of all rule IDs for a policy


Nhiệm vụ cụ thể:

  1. Angular Service — tạo web/src/app/core/services/policy.service.ts
      - Interfaces: PolicySetView, PolicySetSummary, PolicyView, PolicySummary, RuleView,
        ExpressionView, DeletePreview, ReorderRequest + tất cả request types
      - Methods cho PolicySet: getPolicySets, getPolicySetById, createPolicySet, updatePolicySet,
        deletePolicySet, getDeletePreview(id)
      - Methods cho Policy: getPolicies(policySetId), getPolicyById, createPolicy, updatePolicy, deletePolicy
      - Methods cho Rule: getRules(policyId), getRuleById, createRule, updateRule, deleteRule, reorderRules(policyId, orders)
      - Pattern: inject HttpClient, pipe(map(res => res.data)) như resource.service.ts

  2. PolicySetsComponent — tạo web/src/app/dashboard/abac/policy-sets/policy-sets.ts (.html, .css)
      - Table: name, scope, combineAlgorithm, isRoot badge, actions (View, Delete)
      - Nút "+ New Policy Set" mở CreatePolicySetDialogComponent
      - Click row → navigate /admin/abac/policy-sets/:id
      - Delete: gọi preview trước → confirm dialog hiển thị số policies/rules bị xoá

  3. CreatePolicySetDialogComponent — tạo trong policy-sets/create-policy-set-dialog/
      - Form: name (required), scope (select: OPERATOR/TENANT), combineAlgorithm (select), isRoot (checkbox)
      - combineAlgorithm options: DENY_OVERRIDES, PERMIT_OVERRIDES, DENY_UNLESS_PERMIT,
        PERMIT_UNLESS_DENY, FIRST_APPLICABLE, ONLY_ONE_APPLICABLE

  4. PolicySetDetailComponent — tạo web/src/app/dashboard/abac/policy-sets/policy-set-detail/
      - Hiển thị name (read-only), scope, combineAlgorithm (editable)
      - Section "Policies": table name, combineAlgorithm, actions (View, Delete)
      - Nút "+ Add Policy" mở CreatePolicyDialogComponent
      - Click policy row → navigate /admin/abac/policies/:id

  5. CreatePolicyDialogComponent — tạo trong policy-set-detail/create-policy-dialog/
      - Form: name (required), combineAlgorithm (select), targetExpression (textarea — raw SpEL)
      - Hint dưới textarea: "SpEL expression — e.g. resource.name == 'employee'"

  6. PolicyDetailComponent — tạo web/src/app/dashboard/abac/policies/policy-detail/
      - Breadcrumb: Policy Sets / {policySetName} / {policyName}
      - Hiển thị targetExpression (editable textarea), combineAlgorithm
      - Section "Rules": table với drag-handle (order), name, effect badge (PERMIT/DENY), actions (Edit, Delete)
      - Nút "+ Add Rule" mở CreateRuleDialogComponent
      - Reorder: nút ↑ ↓ trên mỗi row (gọi reorderRules sau khi swap)

  7. CreateRuleDialogComponent + EditRuleDialogComponent — tạo trong policy-detail/
      - Form: name (required), description, effect (select PERMIT/DENY),
        targetExpression (textarea — raw SpEL, optional),
        conditionExpression (textarea — raw SpEL, optional)
      - Hint: "Leave empty to match all" cho cả 2 expression fields

  8. Routing — cập nhật app.routes.ts
      - /admin/abac/policy-sets              → PolicySetsComponent
      - /admin/abac/policy-sets/:id          → PolicySetDetailComponent
      - /admin/abac/policies/:id             → PolicyDetailComponent

  9. Sidebar — cập nhật dashboard.html
      - Thêm vào section "ABAC": item icon="account_tree", label="Policy Sets",
        routerLink="abac/policy-sets"

Yêu cầu kỹ thuật:
  - Standalone Components, Signals, Angular Material — pattern giống resources (Batch 1)
  - effect badge PERMIT: màu xanh (--primary-green) / DENY: màu đỏ (--status-error)
  - isRoot badge: chip nhỏ màu accent kế bên PolicySet name
  - No Styling — CSS để trống, Gemini style sau

Output: Liệt kê các file đã tạo/sửa và mã nguồn.
