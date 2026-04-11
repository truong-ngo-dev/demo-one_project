---                                                                                                      Phân tích: ABAC + UIElement dưới Solution 2 (Single Account + Context Switch)

---                                                                                                      Bối cảnh cụ thể hóa

Giả sử hệ thống có 4 portal:

ADMIN portal     → SUPER_ADMIN, PLATFORM_STAFF
OPERATOR portal  → BQL_MANAGER, BQL_STAFF          (scope theo orgId = building)
TENANT portal    → TENANT_ADMIN, TENANT_EMPLOYEE    (scope theo orgId = company)
RESIDENT portal  → RESIDENT                         (scope theo orgId = unit)

Token khi user chọn context OPERATOR:
{
"sub": "person-a",
"activeScope": "OPERATOR",
"activeOrgId": "building-x",
"roles": ["BQL_MANAGER"]
}

  ---
Criterion 1 — Phân quyền (ABAC)

1.1 ResourceDefinition / ActionDefinition — Có duplicate không?

Không cần duplicate. Resource/Action là định danh cho business capability, không phải artifact của     
portal.

resource: user-management   (serviceName: admin)
action: create_user
action: list_users
action: deactivate_user

resource: building-facility (serviceName: admin)
action: view_facility
action: book_facility

Action create_user tồn tại một lần. Nhưng ở ADMIN portal SUPER_ADMIN được phép, còn ở OPERATOR portal  
BQL_MANAGER chỉ được phép tạo user thuộc building của mình — đây là việc của Policy/Rule, không phải   
của Resource/Action.

Kết luận: Resource/Action = shared, không duplicate.

  ---
1.2 PolicySetDefinition — Vấn đề lớn nhất

Design hiện tại:
PolicySetDefinition
scope: Enum: OPERATOR | TENANT   ← thiếu ADMIN, RESIDENT
isRoot: boolean                  ← chỉ có 1 root duy nhất
tenantId: nullable

Dưới Solution 2 cần nhiều root — một per portal scope:

PolicySet A: scope=ADMIN,     isRoot=true   ← platform admin root
PolicySet B: scope=OPERATOR,  isRoot=true   ← tất cả operator buildings
PolicySet C: scope=RESIDENT,  isRoot=true   ← tất cả residents
PolicySet D: scope=TENANT,    isRoot=false, tenantId=null    (template)
PolicySet E: scope=TENANT,    isRoot=false, tenantId="co-z"  (Company Z custom)

Gap nghiêm trọng #1: isRoot: boolean hiện tại implicitly "1 root duy nhất cho toàn hệ thống". Dưới     
Solution 2 cần isRoot per scope — tức invariant hiện tại sai.

Gap nghiêm trọng #2: AdminPolicyProvider.getPolicy(serviceName) hiện load root duy nhất, ignore        
serviceName. Dưới Solution 2 phải thành:

// Hiện tại
AbstractPolicy getPolicy(String serviceName)

// Cần thành
AbstractPolicy getPolicy(String serviceName, String activeScope, String orgId)

Caller (PepEngine) phải truyền activeScope và orgId từ token. Đây là breaking change với libs/abac     
interface.

  ---
1.3 AdminSubjectProvider — Thiếu context

Hiện tại:
Subject { userId, roles: ["BQL_MANAGER"], attributes: {} }

Dưới Solution 2 các Rule SpEL cần check orgId:
// Rule cho BQL_MANAGER chỉ xem user trong building của mình:
subject.roles.contains('BQL_MANAGER') AND subject.getAttribute('orgId') == object.getAttribute('orgId')

attributes: {} không có gì → SpEL trên luôn false → toàn bộ phân quyền theo orgId sẽ sai.

Cần thêm vào Subject:
Subject {
userId: "person-a",
roles: ["BQL_MANAGER"],
attributes: {
"scope":  "OPERATOR",
"orgId":  "building-x"
}
}

AdminSubjectProvider phải extract activeScope và activeOrgId từ JWT claims. Đây là thay đổi bắt buộc.

  ---
1.4 Tenant Self-Authorization (Portal doanh nghiệp tự phân quyền)

Đây là case phức tạp nhất. Company Z muốn tự định nghĩa ai được làm gì trong portal của họ.

Cơ chế cần:

PolicySet E: scope=TENANT, tenantId="company-z", isRoot=false
Policy: "company-z internal rules"
Rule: TENANT_ADMIN of company-z can manage all employees
Rule: TENANT_EMPLOYEE can only view own bookings

Layer authorization: ABAC phải kiểm soát chính nó. Khi TENANT_ADMIN của Company Z vào ABAC admin       
console để edit policy:

// Rule bảo vệ ABAC API endpoint:
subject.roles.contains('TENANT_ADMIN')
AND subject.getAttribute('scope') == 'TENANT'
AND subject.getAttribute('orgId') == targetPolicySet.tenantId

Nếu Company Z chưa tạo custom PolicySet → phải có fallback:
- Option A: Deny everything (an toàn nhưng confusing cho user)
- Option B: Fall back to default TENANT template PolicySet (flexible nhưng phức tạp)

Phản biện: Option B tạo ra "inheritance chain" — tenant policy override template. Đây là một feature   
lớn (giống IAM Role inheritance trong AWS), không phải thêm vài dòng code.

Đánh giá complexity phân quyền:

┌──────────────────────────────────────┬───────────────────────────────────────┐
│                Vấn đề                │            Mức độ thay đổi            │
├──────────────────────────────────────┼───────────────────────────────────────┤
│ isRoot per scope                     │ Breaking invariant, cần migration     │
├──────────────────────────────────────┼───────────────────────────────────────┤
│ getPolicy() signature                │ Breaking change ở libs/abac interface │
├──────────────────────────────────────┼───────────────────────────────────────┤
│ AdminSubjectProvider thêm attributes │ Localized change, dễ                  │
├──────────────────────────────────────┼───────────────────────────────────────┤
│ Tenant self-authorization            │ Feature lớn, cần thiết kế riêng       │
├──────────────────────────────────────┼───────────────────────────────────────┤
│ PolicySet fallback/inheritance       │ Rất phức tạp, có thể deferrable       │
└──────────────────────────────────────┴───────────────────────────────────────┘

  ---
Criterion 2 — UIElement Display

2.1 UIElement hiện tại không có scope

UIElement {
elementId: "btn_create_user"   ← unique globally
type: BUTTON
resourceId: → user-management
actionId:   → create_user
}

Không có field nào nói element này thuộc portal nào. Dưới Solution 2:

- Admin portal có button Create User
- Operator portal cũng có button Invite Resident — cùng action create_user nhưng khác portal, khác     
  label, khác UX context

Đây là 2 UIElement khác nhau (khác elementId), nhưng cùng trỏ về 1 Action. Đây không phải duplicate —  
đây là 2 UI artifact cho cùng 1 capability nhưng ở 2 surface khác nhau. Hoàn toàn hợp lý về mặt        
semantic.

Vấn đề: Khi FE Operator portal hỏi "evaluate navigation cho tôi", nó nhận về danh sách tất cả
UIElements trong DB (kể cả của Admin portal). Không có cơ chế nào filter "chỉ lấy UIElements của       
OPERATOR scope".

Cần thêm:
UIElement {
+ scope: Enum(ADMIN | OPERATOR | TENANT | RESIDENT)  ← new required field
elementId: "btn_operator_invite_resident"
...
}

Và SimulateNavigation API phải nhận activeScope để filter:
GET /abac/navigate?scope=OPERATOR&orgId=building-x
→ chỉ evaluate UIElements where scope=OPERATOR

  ---
2.2 Tenant portal — UIElement tự định nghĩa?

Trường hợp phức tạp: Company Z có portal riêng, họ muốn tự định nghĩa UI elements cho nội bộ — không   
chỉ tự phân quyền (PolicySet) mà còn tự quyết định UI component nào visible.

Hai approach:

Approach A — Platform định nghĩa UIElements, Tenant chỉ define Policy:
UIElement: btn_tenant_view_booking  (scope=TENANT, elementId fixed)
Tenant tự viết Policy Rule: ai trong company-z được thấy btn này
Đơn giản hơn, Apartcom control UI surface. Tenant chỉ control permissions.

Approach B — Tenant có thể tạo UIElements:
Tenant admin tạo UIElement mới:
elementId: "btn_coz_approve_expense"
scope: TENANT
orgId: "company-z"   ← thêm orgId vào UIElement
resourceId: → expense-management
actionId:   → approve
Mạnh hơn nhưng UIElement cần thêm orgId field. FE phải dynamic load elementIds thay vì hardcode.

Phản biện Approach B: FE hiện tại hardcode elementId — đây là invariant design ("frontend hardcodes    
this"). Nếu tenant tạo UIElements, FE tenant-portal phải dynamic fetch danh sách elementIds trước, sau
đó mới evaluate. Đây là thay đổi kiến trúc FE significant.

  ---
2.3 SimulateNavigation — hiện tại không context-aware

Hiện tại SimulateNavigation load toàn bộ UIElements, evaluate tất cả với Subject. Dưới Solution 2:

// Step 1: filter UIElements by activeScope (và orgId nếu scope=TENANT)
// Step 2: load PolicySet matching activeScope (và tenantId nếu scope=TENANT)
// Step 3: evaluate mỗi UIElement với Subject có attributes {scope, orgId}
// Step 4: return visibility map

Step 1 + Step 2 là 2 breaking changes so với implementation hiện tại.

Thêm edge case: User switch từ OPERATOR sang RESIDENT trong cùng session. FE phải:
1. Invalidate cached UIElement visibility map cũ
2. Fetch lại UIElements cho scope=RESIDENT
3. Re-evaluate navigation
4. Re-render toàn bộ nav

Nếu FE cache visibility map (để tránh round-trip mỗi khi render), cache invalidation khi switch context
là mandatory — và nếu làm sai, user có thể thấy OPERATOR buttons khi đang ở RESIDENT context
(security/UX bug).

  ---
Tổng hợp & Kết luận

Ma trận thay đổi

┌─────────────────────────────────┬────────────────────────────────────────┬──────────────────────┐    
│            Component            │           Thay đổi cần thiết           │        Mức độ        │    
├─────────────────────────────────┼────────────────────────────────────────┼──────────────────────┤    
│ PolicySetDefinition.isRoot      │ Per-scope root invariant               │ Breaking             │    
├─────────────────────────────────┼────────────────────────────────────────┼──────────────────────┤    
│ PolicySetDefinition.scope enum  │ Thêm ADMIN, RESIDENT                   │ Migration            │    
├─────────────────────────────────┼────────────────────────────────────────┼──────────────────────┤    
│ AdminPolicyProvider.getPolicy() │ Thêm scope + orgId params              │ Breaking (libs/abac) │    
├─────────────────────────────────┼────────────────────────────────────────┼──────────────────────┤    
│ AdminSubjectProvider            │ Thêm scope + orgId vào attributes      │ Localized            │    
├─────────────────────────────────┼────────────────────────────────────────┼──────────────────────┤    
│ UIElement.scope                 │ Field mới bắt buộc                     │ Migration + data     │    
├─────────────────────────────────┼────────────────────────────────────────┼──────────────────────┤    
│ UIElement.orgId                 │ Nếu tenant tự tạo UIElement            │ Optional feature     │    
├─────────────────────────────────┼────────────────────────────────────────┼──────────────────────┤    
│ SimulateNavigation              │ Filter by scope, load scoped PolicySet │ Significant refactor │    
├─────────────────────────────────┼────────────────────────────────────────┼──────────────────────┤    
│ Tenant self-auth API guard      │ ABAC bảo vệ ABAC console               │ New logic            │    
├─────────────────────────────────┼────────────────────────────────────────┼──────────────────────┤    
│ PolicySet fallback/inheritance  │ Tenant chưa có policy                  │ Complex, deferrable  │    
└─────────────────────────────────┴────────────────────────────────────────┴──────────────────────┘

Đánh giá tổng thể

Về phân quyền:
Complexity thực sự không nằm ở việc "viết thêm rule" mà ở việc libs/abac interface hiện tại không thiết
kế cho multi-scope. PolicyProvider và SubjectProvider đều đang single-context. Breaking change ở đây
lan ra cả libs/abac (shared library), ảnh hưởng tất cả services dùng nó.

Về UIElement:
Thiếu scope field là gap design rõ ràng — không thể serve multi-portal mà không có nó. Thêm field này  
là migration nhỏ nhưng nó kéo theo thay đổi ở SimulateNavigation, coverage API, và FE query pattern.

Về tenant self-authorization:
Đây là feature độc lập, không nên bundle vào cùng Solution 2 migration. Có thể defer sau khi
multi-scope core đã ổn định. Nếu làm sớm thì Approach A (platform define UIElements, tenant define     
Policy) ít rủi ro hơn Approach B.

Recommendation thứ tự triển khai:
1. Extend PolicySetDefinition.scope enum + fix isRoot per scope invariant
2. Update AdminSubjectProvider để inject scope + orgId vào Subject.attributes
3. Update AdminPolicyProvider.getPolicy() signature (breaking change cần coordinate với libs/abac)
4. Add scope field vào UIElement + migration
5. Update SimulateNavigation để filter by scope
6. Sau đó mới làm tenant self-authorization (Approach A trước)


Phân tích: Inject Context Attributes vào Token — Solution 2
                                                                                                         
---                         
Định nghĩa bài toán                                                                                       
Token cần phải carry:                                                                                  
{               
"sub": "person-a",                                                                                       "activeScope": "OPERATOR",                                                                           
"activeOrgId": "building-x",
"roles": ["BQL_MANAGER"]
}

Câu hỏi là: data này đến từ đâu, được inject vào lúc nào, bởi ai?

Và điều kiện tiên quyết cần giải quyết trước: OAuth2 service phải biết user X có những contexts nào    
available — thông tin này nằm ở admin service (User → Role → scope/orgId mapping). Mọi approach đều    
phải giải quyết dependency này.

  ---
3 Approach chính

  ---
Approach A — Token Customizer tại issue time (inject trong OAuth2 flow)

User chọn context trước khi token được issue, context được truyền vào OAuth2 authorization request như
additional parameter.

[FE] → GET /oauth2/authorize?...&active_scope=OPERATOR&active_org_id=building-x
↓
[OAuth2 Server] TokenCustomizer:
1. Đọc active_scope + active_org_id từ authorization request
2. Gọi AdminService: validateUserContext(userId, scope, orgId) → roles
3. Inject vào JWT: activeScope, activeOrgId, roles
↓
[FE] nhận access token đã có đủ context

Context switch = trigger lại OAuth2 flow với param khác, hoặc dùng refresh token endpoint kèm param    
mới.

Complexity:
- Spring Authorization Server hỗ trợ OAuth2TokenCustomizer<JwtEncodingContext> — đây là extension point
  chính thức, không cần hack.
- Tuy nhiên, additional_parameters trong authorization request bình thường bị strip đi trước khi đến   
  TokenCustomizer. Cần custom AuthenticationConverter + AuthenticationProvider để forward params này.
- OAuth2 service phải gọi admin service để validate context → HTTP client, error handling, circuit     
  breaker.
- Context switch logic: nếu dùng refresh token, phải custom OAuth2RefreshTokenAuthenticationProvider để
  đọc param mới.

Performance:
- Một lần HTTP call từ OAuth2 → admin service tại mỗi token issuance.
- Token issuance không xảy ra thường xuyên (chỉ khi login hoặc switch context) — acceptable.
- Không có overhead per-request sau khi token đã được issue.

Stability:
- Nếu admin service down tại thời điểm login → OAuth2 token issuance fail → user không login được. Hard
  coupling tại authentication time.
- Admin service là SPOF cho toàn bộ authentication flow.
- Data trong token bị stale: nếu admin revoke role của user sau khi token issued, token vẫn carry roles
  cũ đến khi expire. Không giải quyết được nếu không có introspection/revocation.

  ---
Approach B — Two-Phase Auth (bare token → context switch endpoint)

Tách authentication và context selection thành 2 bước riêng biệt.

Phase 1: Login
[FE] → POST /oauth2/token (standard flow)
[OAuth2 Server] → issue "bare" identity token
{
"sub": "person-a",
"availableContexts": [           ← embed sẵn available contexts
{"scope": "OPERATOR", "orgId": "building-x", "roles": ["BQL_MANAGER"]},
{"scope": "RESIDENT",  "orgId": "unit-10b",   "roles": ["RESIDENT"]}
]
}

Phase 2: Context Selection
[FE hiển thị context selector dựa trên availableContexts trong token]
[FE] → POST /auth/context/activate {scope: "OPERATOR", orgId: "building-x"}
+ Authorization: Bearer <bare_token>
[OAuth2 Server] validate: context này có trong availableContexts không?
→ issue scoped token:
{
"sub": "person-a",
"activeScope": "OPERATOR",
"activeOrgId": "building-x",
"roles": ["BQL_MANAGER"]
}

Context switch = gọi lại /auth/context/activate với context khác.

Complexity:
- Cần implement /auth/context/activate endpoint — không phải standard OAuth2, là custom endpoint.
- FE phải handle 2 loại token (bare và scoped) và biết khi nào dùng cái nào.
- availableContexts trong bare token phải được sign/verify — không thể để FE tự tạo.
- Admin service call vẫn cần tại Phase 1 để load availableContexts.
- Phức tạp hơn Approach A về số lượng moving parts, nhưng mỗi part rõ ràng hơn.

Performance:
- Phase 1: 1 admin service call để load all available contexts.
- Phase 2: validation thuần token (không cần DB call nếu availableContexts đã trong bare token).
- Context switch chỉ cần re-sign token với active context khác — nhanh hơn Approach A khi switch.
- Tradeoff: token payload lớn hơn (chứa availableContexts).

Stability:
- Admin service chỉ cần available tại Phase 1 (login). Phase 2 (context switch) hoàn toàn offline với  
  admin service.
- Nếu admin service down, user đã login vẫn switch context được → graceful degradation tốt hơn Approach
  A.
- availableContexts cũng có thể stale — nếu admin thêm role mới cho user sau khi login, user phải      
  re-login để thấy context mới. Acceptable behavior.
- Cần định nghĩa rõ bare token scope: nó được phép gọi API nào? Nếu không giới hạn, là security hole.

  ---
Approach C — Context qua request, không trong token

Token chỉ chứa identity. Context được truyền kèm mỗi request qua header, resource server validate và   
resolve quyền tương ứng.

Token:
{ "sub": "person-a" }

Request:
GET /api/users
Authorization: Bearer <token>
X-Active-Context: OPERATOR:building-x

Resource Server:
1. Validate token (sig, exp)
2. Đọc X-Active-Context header
3. Validate: có user này thực sự có OPERATOR role tại building-x? → query admin service
4. Build Subject với roles + attributes
5. Evaluate policy

Complexity:
- Token đơn giản nhất.
- Nhưng resource server phải resolve context mỗi request — gọi admin service hoặc cache.
- Custom header X-Active-Context phải được validate nghiêm ngặt — FE không được tự ý đặt giá trị tùy   
  tiện.
- Security model phức tạp hơn: ai chịu trách nhiệm validate context? Mỗi resource server phải implement
  logic này.
- Không thể dùng JWT validation đơn thuần — luôn cần thêm một bước runtime validation.

Performance:
- Tệ nhất trong 3 approach: mỗi request đều có thể cần 1 DB/service call để validate context.
- Cache có thể giảm thiểu nhưng tạo thêm stale data problem và cache invalidation complexity.
- Với distributed service, mỗi service phải có cache riêng hoặc phải gọi admin service riêng.

Stability:
- Admin service là dependency tại runtime cho mọi request — nếu admin service slow/down, toàn bộ hệ    
  thống bị ảnh hưởng.
- Không có token-level guarantee về context — hoàn toàn phụ thuộc runtime validation.
- Nếu cache được dùng, revocation delay lớn hơn.

  ---
Ma trận đánh giá

┌─────────────┬──────────────────────────────┬─────────────────────────┬───────────────────────────┐   
│             │          Approach A          │       Approach B        │        Approach C         │   
├─────────────┼──────────────────────────────┼─────────────────────────┼───────────────────────────┤   
│             │ Trung bình — cần custom      │ Cao — 2 phase, custom   │ Thấp ở token design,      │   
│ Complexity  │ OAuth2 params forwarding,    │ endpoint, 2 token types │ nhưng cao ở runtime       │   
│             │ nhưng single flow            │                         │ validation mỗi request    │   
├─────────────┼──────────────────────────────┼─────────────────────────┼───────────────────────────┤   
│             │ Tốt — 1 call lúc issue, 0    │ Tốt — call lúc login,   │ Tệ — potential call       │   
│ Performance │ overhead per-request         │ context switch gần như  │ per-request, cần cache    │   
│             │                              │ free                    │                           │   
├─────────────┼──────────────────────────────┼─────────────────────────┼───────────────────────────┤   
│             │ Rủi ro cao — admin service   │ Tốt — admin service chỉ │ Rủi ro cao nhất — admin   │   
│ Stability   │ là SPOF tại authentication   │  cần lúc login, context │ service là SPOF tại       │   
│             │                              │  switch độc lập         │ runtime                   │   
└─────────────┴──────────────────────────────┴─────────────────────────┴───────────────────────────┘

  ---
Kết luận

Approach B là lựa chọn tốt nhất cho Solution 2.

Lý do:
1. Separation of concerns rõ ràng nhất: authentication và context selection là 2 concerns khác nhau,   
   tách thành 2 bước là correct về mặt design.
2. Stability tốt nhất: context switch không phụ thuộc admin service — user đã login không bị ảnh hưởng
   nếu admin service có sự cố.
3. Performance chấp nhận được: overhead chỉ tại login, zero overhead per-request sau đó.
4. Security clean: context được sign trong bare token, không thể forge từ FE.

Điểm cần chốt thêm khi chọn Approach B:
- Bare token scope: phải giới hạn — chỉ được gọi /auth/context/activate và một số endpoint public,     
  không được gọi business API.
- availableContexts TTL: bao lâu thì user phải re-login để refresh? Hay có webhook/event để invalidate
  sớm khi admin thay đổi role?
- Context switch rate limit: cần không để tránh abuse?

