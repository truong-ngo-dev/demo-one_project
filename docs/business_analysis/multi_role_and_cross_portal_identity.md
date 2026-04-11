Phân tích — Multi-Role & Cross-Portal Identity

--- Framing vấn đề

Trước khi đi vào từng solution, cần định nghĩa rõ những gì cần giải quyết:

Một "người thực" có thể tồn tại trong nhiều "context" khác nhau trong hệ thống:

    Người A:
      - Là Super Admin của platform Apartcom
      - Là BQL Manager của Tòa nhà X
      - Là Cư dân căn hộ 10B tại Tòa nhà Y
      - Là Nhân viên của Công ty Z (thuê văn phòng tại Tòa nhà X)

    Câu hỏi: Hệ thống nhìn nhận người này như thế nào?
    Phân quyền + Navigation hoạt động ra sao?

  ---
Solution 1 — Separate Accounts Per Portal

Mỗi portal / context yêu cầu tài khoản riêng biệt.

Người A (thực tế):
Account 1: a.admin@apartcom.vn      → Admin Portal, role: SUPER_ADMIN
Account 2: a.manager@building-x.vn → Operator Portal, role: BQL_MANAGER
Account 3: a.resident@personal.com → Resident Portal, role: RESIDENT
Account 4: a@company-z.vn          → Tenant Portal, role: TENANT_EMPLOYEE

Tại sao có thể hợp lý:
Nhiều enterprise system làm vậy — email công ty vs email cá nhân là hai danh tính thực sự tách biệt.  
Người dùng đã quen với việc switch giữa các tài khoản (Google multi-account, Slack workspaces).

Phản biện:

- Khi người rời tòa nhà / công ty, admin phải deactivate N accounts thay vì 1. Bỏ sót một account là  
  lỗ hổng security thực sự.
- Audit trail bị phân mảnh — không thể nói "Người A đã làm gì trong hệ thống hôm nay" mà phải tổng hợp
  từ N accounts.
- SSO với social provider (Google, Microsoft) gặp vấn đề: một email → nhiều accounts? Hay mỗi account
  cần email riêng?
- Với khách hàng nhỏ (tòa nhà nhỏ, BQL manager cũng là cư dân), forcing 2 accounts là friction không  
  cần thiết.
- Scale problem: một platform với 100 tòa nhà, mỗi tòa có 50 BQL staff vừa là cư dân → 100 × 50 =     
  5,000 account "thừa" chỉ để giải quyết limitation của model.

Ảnh hưởng đến ABAC:

// AdminSubjectProvider: đơn giản nhất có thể
Subject {
userId: "acc-001",          // account-level ID
roles: ["BQL_MANAGER"],     // chỉ roles của account này
attributes: { orgId: "building-x" }
}

// PolicyProvider: load đúng 1 PolicySet theo account type
// Không cần context, không cần switching
AbstractPolicy getPolicy(String serviceName);  // current signature — đủ dùng

Không có complexity nào cả. Token đơn giản, evaluate đơn giản, không có edge case.

Ảnh hưởng đến UIElement / Navigation:

FE Admin Portal:    hardcode nav structure → evaluate → render
FE Operator Portal: hardcode nav structure → evaluate → render
// Không cần context switcher, không cần dynamic nav reload

Mỗi portal là một app độc lập, UIElement set riêng, không overlap.

  ---
Solution 2 — Single Account, Multiple Roles, Context Switching

Một tài khoản, nhiều vai trò, user chọn context khi đăng nhập hoặc switch trong session.

Account: person-a@apartcom.vn
Roles available: [SUPER_ADMIN, BQL_MANAGER@building-x, RESIDENT@unit-10b, EMPLOYEE@company-z]

Login → Context selector:
[ ] Admin Portal (SUPER_ADMIN)
[ ] Operator Portal - Tòa nhà X (BQL_MANAGER)
[ ] Resident Portal - Căn hộ 10B (RESIDENT)
[ ] Tenant Portal - Công ty Z (EMPLOYEE)

User chọn 1 → token issued với activeContext

Tại sao có thể hợp lý:
Phản ánh đúng thực tế — người thực chỉ có một danh tính. Google Workspace, Microsoft Entra, và hầu hết
enterprise IdP hiện đại đều đi theo hướng này. "One identity, multiple contexts" là hướng của ngành.

Phản biện:

- Token design problem: Token phải carry activeScope + activeOrgId + roles của context đó. Ai populate
  data này? OAuth2 service cần gọi admin service để lấy danh sách contexts available cho user → service
  coupling tại authentication time.
- Context switch UX: Nếu switch context yêu cầu re-issue token (round trip đến OAuth2 server) →       
  latency. Nếu chỉ là frontend state → token không phản ánh đúng context → ABAC evaluate sai.
- Session edge case: User đang làm việc ở Operator Portal, token expire, refresh token → context có   
  được preserve không?
- Security risk tinh tế: User có cả SUPER_ADMIN và RESIDENT context. Nếu XSS attack steal token đang ở
  RESIDENT context → attacker có thể switch sang SUPER_ADMIN context không? Phụ thuộc vào
  implementation.
- Complexity của SubjectProvider: Phải build Subject chỉ từ active context, không được lẫn lộn roles  
  của context khác.

Ảnh hưởng đến ABAC:

// Token payload phức tạp hơn nhiều
{
"sub": "person-a",
"activeScope": "OPERATOR",
"activeOrgId": "building-x",
"roles": ["BQL_MANAGER"],          // chỉ roles của OPERATOR context
"availableContexts": [...]         // optional — để FE show context switcher
}

// AdminSubjectProvider: phải đọc activeScope từ token
Subject {
userId: "person-a",
roles: ["BQL_MANAGER"],            // từ active context only
attributes: {
scope: "OPERATOR",
orgId: "building-x"
}
}

// PolicyProvider: phải context-aware (Vấn đề 6 trong analysis doc)
AbstractPolicy getPolicy(String serviceName, PolicyEvaluationContext ctx);
//   ctx.activeScope = "OPERATOR" → load OPERATOR PolicySet

Nếu SubjectProvider load nhầm roles từ context khác → toàn bộ ABAC evaluate sai. Đây là lỗi silent —  
không throw exception, chỉ cho/từ chối access sai.

Ảnh hưởng đến UIElement / Navigation:

User switch context OPERATOR → RESIDENT:
1. Re-issue token với activeScope=RESIDENT
2. FE reload navigation với RESIDENT UIElement set
3. Gọi evaluate lại với RESIDENT elements
4. Render nav mới

// Nav không còn static — phải dynamic theo active context
// Context switcher UI component là required, không optional

UIElement vẫn không duplicate (đã phân tích trước) nhưng FE phức tạp hơn đáng kể.

  ---
Solution 3 — Identity Token + Context Token (Token Exchange)

Tách biệt hoàn toàn danh tính và authorization context thành hai loại token.

[Step 1] Login → Identity Token (long-lived, chỉ chứa identity)
{
"sub": "person-a",
"email": "a@apartcom.vn",
"type": "identity"
}

[Step 2] User chọn context → Token Exchange (RFC 8693)
→ Context Token (short-lived, scoped)
{
"sub": "person-a",
"scope": "OPERATOR",
"orgId": "building-x",
"roles": ["BQL_MANAGER"],
"exp": "1h",               // context token expire nhanh hơn identity token
"type": "context"
}

[Step 3] Mọi API call dùng Context Token

Pattern này tương tự AWS STS AssumeRole.

Tại sao có thể hợp lý:
Cleanest separation of concerns. Identity token không bao giờ thay đổi — người vẫn là người. Context  
token thay đổi khi switch. High-privilege context (SUPER_ADMIN) có thể có TTL ngắn hơn (15 phút) trong
khi RESIDENT có TTL dài hơn (8 giờ) — risk-appropriate expiry.

Phản biện:

- Phức tạp nhất về implementation: cần Token Exchange endpoint, FE phải manage 2 loại token.
- Thêm round trip: mỗi lần switch context = 1 request đến OAuth2 server.
- Refresh logic phức tạp: refresh identity token và context token độc lập, hay cùng nhau?
- Đây là RFC 8693 — không phải mọi OAuth2 library đều support sẵn.
- Overkill cho hệ thống ở scale early-to-mid. Chỉ thực sự cần khi security requirement rất cao (ngân  
  hàng, healthcare).

Ảnh hưởng đến ABAC:

Giống Solution 2 nhưng cleaner hơn vì context token là source of truth duy nhất. SubjectProvider chỉ  
cần đọc context token — không cần query DB để resolve context.

Ảnh hưởng đến UIElement / Navigation:

Giống Solution 2. Complexity không giảm ở FE.

  ---
Solution 4 — Invitation / Workspace Model

Người dùng có một master identity, được "mời" vào các organization như thành viên. Mỗi membership tạo
ra một context độc lập.

Master Identity: person-a (linked đến email / social provider)
└── Member of: Apartcom Platform  (role: SUPER_ADMIN)
└── Member of: Building X / BQL   (role: BQL_MANAGER)
└── Member of: Building Y / Unit  (role: RESIDENT)
└── Member of: Company Z          (role: EMPLOYEE)

Khi login:
→ Authenticate với master identity
→ Chọn "workspace" / membership để activate
→ Token issued cho membership đó

Đây là model của Slack, Notion, Linear, Vercel.

Tại sao có thể hợp lý:
Phù hợp nhất với enterprise SaaS multi-tenant. Mỗi "khách hàng" (tòa nhà / công ty) là một workspace  
độc lập. Onboarding = invite người vào workspace. Offboarding = remove khỏi workspace. Identity       
provider vẫn là một. Scale tốt khi có nhiều đơn vị mua phần mềm.

Phản biện:

- Về bản chất, đây là Solution 2 với tên gọi khác ("context" vs "workspace/membership"). Complexity   
  tương đương.
- Cần xây dựng invitation system (invite email, accept flow, expiry) — thêm scope đáng kể.
- Nếu platform Apartcom là người quản lý toàn bộ user (không phải user tự register), thì invitation   
  model có thể over-engineer.
- Ranh giới giữa "BQL của tòa nhà X" và "BQL của tòa nhà Y" phải được enforce ở organization level —  
  nếu model organization chưa có, phải build thêm.

Ảnh hưởng đến ABAC:

// Membership record trở thành context carrier
Membership {
personId: "person-a",
orgId: "building-x",
orgType: OPERATOR,
roles: ["BQL_MANAGER"]
}

// Token khi activate membership
{
"sub": "person-a",
"membershipId": "mem-001",
"orgId": "building-x",
"scope": "OPERATOR",
"roles": ["BQL_MANAGER"]
}

PolicyProvider load PolicySet dựa trên scope + orgId trong token. Clean và predictable.

Ảnh hưởng đến UIElement / Navigation:

Membership activation = context switch. Nav reload theo membership. Giống Solution 2 về complexity    
phía FE.

  ---
Solution 5 — Hybrid: Default Separate Accounts + Optional Identity Linking

Mặc định mỗi portal dùng account riêng (simplicity của Solution 1). Nhưng platform cho phép user tự   
"link" các accounts về một master identity nếu muốn.

Default (đơn giản):
admin@apartcom.vn     → Admin Portal
a@building-x.vn      → Operator Portal
(hoàn toàn độc lập)

Optional linking (khi user muốn):
admin@apartcom.vn ──┐
a@building-x.vn   ──┼── linked to master: person-a@gmail.com
a@company-z.vn    ──┘
→ Single sign-on, context selector khi login qua master identity

Tại sao có thể hợp lý:
Cho phép bắt đầu đơn giản, scale lên khi cần. Khách hàng nhỏ không cần linking. Khách hàng enterprise
có thể enable. Đây là cách nhiều SaaS hiện đại approach — start simple, add sophistication
progressively.

Phản biện:

- Hai code path song song: linked flow và non-linked flow. Test matrix tăng gấp đôi.
- User experience không nhất quán: một số user có context switcher, một số không.
- Khi linked accounts có conflict (email bị dùng ở cả hai accounts), resolution logic phức tạp.
- "Optional" feature thường trở thành "required" sau khi một số user dùng — technical debt
  accumulates.

Ảnh hưởng đến ABAC và UIElement:

Complexity = max(Solution 1, Solution 2) — phải support cả hai path.

  ---
Ma trận đánh giá

Tiêu chí 1: Mức độ Generic

Đáp ứng được nhiều loại khách hàng khác nhau — từ nhỏ đến lớn, từ đơn giản đến phức tạp.

┌────────────────────────────┬───────┬────────────────────────────────────────────────────────────┐   
│          Solution          │ Score │                           Lý do                            │   
├────────────────────────────┼───────┼────────────────────────────────────────────────────────────┤   
│ 1 — Separate Accounts      │ 2/5   │ Hoạt động tốt khi mỗi người chỉ có 1 vai. Friction cao khi │   
│                            │       │  1 người có nhiều vai — loại bỏ nhiều use case thực tế     │   
├────────────────────────────┼───────┼────────────────────────────────────────────────────────────┤   
│ 2 — Single Account +       │ 4/5   │ Phản ánh đúng thực tế. Phù hợp từ khách hàng nhỏ đến lớn.  │   
│ Context Switch             │       │ Cần UX tốt cho context selector                            │   
├────────────────────────────┼───────┼────────────────────────────────────────────────────────────┤   
│ 3 — Token Exchange         │ 3/5   │ Enterprise-grade nhưng có thể overkill cho KH nhỏ. Không   │   
│                            │       │ có gì KH nhỏ được lợi thêm từ RFC 8693                     │   
├────────────────────────────┼───────┼────────────────────────────────────────────────────────────┤   
│ 4 — Invitation/Workspace   │ 5/5   │ Phù hợp nhất với SaaS multi-tenant. Scale tốt nhất khi có  │   
│                            │       │ nhiều "đơn vị" mua phần mềm                                │   
├────────────────────────────┼───────┼────────────────────────────────────────────────────────────┤   
│ 5 — Hybrid                 │ 3/5   │ Flexible nhưng complexity ẩn cao. "Generic" nhưng          │   
│                            │       │ inconsistent experience                                    │   
└────────────────────────────┴───────┴────────────────────────────────────────────────────────────┘

Tiêu chí 2: Mức độ phức tạp triển khai Phân quyền + UI

Thấp = dễ implement và maintain.

┌─────────────────────────┬────────────────────────────────────┬──────────────────────────┬──────┐    
│        Solution         │          ABAC Complexity           │ UI/Navigation Complexity │ Tổng │    
├─────────────────────────┼────────────────────────────────────┼──────────────────────────┼──────┤    
│ 1 — Separate Accounts   │ Thấp — token đơn giản,             │ Thấp — mỗi app static    │ Thấp │    
│                         │ SubjectProvider không cần context  │ nav                      │      │    
├─────────────────────────┼────────────────────────────────────┼──────────────────────────┼──────┤    
│                         │ Cao — PolicyProvider phải          │ Cao — context switcher   │      │    
│ 2 — Single Account +    │ context-aware, SubjectProvider chỉ │ UI, dynamic nav reload,  │ Cao  │    
│ Context Switch          │  load active context roles,        │ manage token lifecycle   │      │    
│                         │ re-issue token khi switch          │                          │      │    
├─────────────────────────┼────────────────────────────────────┼──────────────────────────┼──────┤    
│                         │ Rất cao — 2 loại token, Token      │                          │ Rất  │    
│ 3 — Token Exchange      │ Exchange endpoint, refresh logic   │ Cao (tương đương S2)     │ cao  │    
│                         │ phức tạp                           │                          │      │    
├─────────────────────────┼────────────────────────────────────┼──────────────────────────┼──────┤    
│ 4 —                     │ Cao (tương đương S2) + thêm        │ Cao (tương đương S2)     │ Cao  │    
│ Invitation/Workspace    │ invitation system                  │                          │      │    
├─────────────────────────┼────────────────────────────────────┼──────────────────────────┼──────┤    
│ 5 — Hybrid              │ Rất cao — support 2 code path      │ Rất cao — 2 flow khác    │ Rất  │    
│                         │                                    │ nhau                     │ cao  │    
└─────────────────────────┴────────────────────────────────────┴──────────────────────────┴──────┘

Tổng hợp

                  Generic  ABAC+UI Complexity
Solution 1          ██░░░       ██░░░  (thấp = tốt)
Solution 2          ████░       ████░
Solution 3          ███░░       █████
Solution 4          █████       ████░
Solution 5          ███░░       █████

  ---
Khuyến nghị

Không có solution nào tối ưu tuyệt đối. Nhưng với tư duy enterprise SaaS scale:

Giai đoạn hiện tại → Solution 2

Context switching là đủ. Không cần invitation system (chưa có org model), không cần RFC 8693 (chưa có
high-security requirement). Phần cốt lõi cần làm đúng ngay:

1. Token phải carry activeScope + activeOrgId + roles của context đó (only)
2. SubjectProvider load từ token — không query lại DB để resolve context
3. PolicyProvider.getPolicy() nhận PolicyEvaluationContext
4. FE có context selector sau login, nav reload khi switch

Thiết kế để migrate lên Solution 4 sau

Khi platform có nhiều "đơn vị" (nhiều tòa nhà, nhiều công ty) mua phần mềm, invitation model trở nên  
cần thiết. Nhưng nếu data model và token structure của Solution 2 được thiết kế đúng, migration sang  
Solution 4 chủ yếu là thêm invitation flow — không phải rewrite authorization layer.

Solution 2 → Solution 4:
Thêm: Invitation system (invite, accept, revoke membership)
Giữ nguyên: Token structure, SubjectProvider, PolicyProvider, UIElement model
Thay đổi nhỏ: Context selection UI (từ "chọn role" → "chọn workspace")

Đây là lý do cần làm đúng Solution 2 ngay từ đầu thay vì chọn Solution 1 cho đơn giản — cost của      
migration từ 1 → 4 cao hơn nhiều so với 2 → 4.


Separate OAuth2 Client Per Portal vs Shared Client

---                                                                                                     Định nghĩa hai approach

Approach A — One Client Per Portal                                                                    
Spring Authorization Server registered clients:

client_id: admin-portal
scopes: [openid, profile, scope:admin]
redirect_uris: [https://admin.apartcom.vn/callback]
    token_ttl: 15m (high privilege → short lived)

client_id: operator-portal
scopes: [openid, profile, scope:operator]
redirect_uris: [https://operator.apartcom.vn/callback]
    token_ttl: 8h

client_id: tenant-portal
scopes: [openid, profile, scope:tenant]
redirect_uris: [https://tenant.apartcom.vn/callback]
    token_ttl: 8h

client_id: resident-portal
scopes: [openid, profile, scope:resident]
redirect_uris: [https://resident.apartcom.vn/callback]
    token_ttl: 12h

Approach B — Shared Client
client_id: apartcom-web
scopes: [openid, profile, scope:admin, scope:operator, scope:tenant, scope:resident]
redirect_uris: [
https://admin.apartcom.vn/callback,
https://operator.apartcom.vn/callback,
https://tenant.apartcom.vn/callback,
https://resident.apartcom.vn/callback
]
token_ttl: ??? (không thể set khác nhau per portal)

  ---
Phân tích Approach A — Separate Clients

Context determination flow:

User → Operator Portal → /authorize?client_id=operator-portal&scope=scope:operator
↓
AS nhận client_id=operator-portal
→ biết đây là Operator Portal
→ filter contexts của user: chỉ show OPERATOR contexts
→ User thấy: [Building X - BQL Manager, Building Y - BQL Technical]
→ User chọn Building X
→ Token: { sub, scope:"operator", orgId:"building-x", roles:["BQL_MANAGER"] }

Token ra khỏi AS đã được scoped sẵn theo portal — không cần thêm logic ở downstream.

Lợi thế trực tiếp đến ABAC:

// PolicyProvider nhận token → đọc scope → load đúng PolicySet ngay
// Không cần logic suy luận, không cần query thêm

Token claims:
scope: "operator"           → load OPERATOR PolicySet
orgId: "building-x"         → scoped to org
roles: ["BQL_MANAGER"]      → subject roles

// SubjectProvider: đọc thẳng từ token claims
Subject {
userId: "person-a",
roles: ["BQL_MANAGER"],
attributes: { scope: "OPERATOR", orgId: "building-x" }
}

Phản biện — tại sao không phải silver bullet:

- Khi business yêu cầu "super admin có thể xem operator portal để support", phải re-authenticate với  
  operator-portal client_id. Cross-portal navigation không seamless.
- Mỗi client cần quản lý riêng: secret rotation, scope list, redirect_uri whitelist. Khi thêm portal  
  mới = thêm client mới + config thay đổi ở AS.
- Nếu mobile app cần access nhiều portal contexts trong cùng một app shell (e.g., single mobile app   
  cho cả resident lẫn tenant employee) → phải xử lý multiple clients trong một app, phức tạp hơn.

  ---
Phân tích Approach B — Shared Client

Context determination flow:

User → Operator Portal → /authorize?client_id=apartcom-web&scope=openid profile
↓
AS nhận client_id=apartcom-web
→ KHÔNG biết user đang ở portal nào
→ Phải show ALL contexts của user
→ User thấy: Admin, Operator (Building X), Operator (Building Y),
Resident (Unit 10B), Tenant Employee (Company Z)
→ User chọn
→ Token: { sub, activeScope:"operator", orgId:"building-x", roles:["BQL_MANAGER"] }

Vấn đề: user đang ở Operator Portal URL nhưng AS không biết điều đó. Nếu user chọn "Admin" context →  
token có scope:admin nhưng redirect về operator.apartcom.vn → mismatch.

Giải pháp workaround thường thấy:

Option 1: Dùng redirect_uri để AS suy ra portal
redirect_uri=https://operator.apartcom.vn/callback
→ AS parse domain → "operator" → filter contexts
→ Fragile: domain đổi thì logic vỡ

Option 2: Thêm custom parameter
/authorize?client_id=apartcom-web&portal_hint=operator
→ AS đọc portal_hint → filter contexts
→ Non-standard, không có trong OAuth2 spec

Option 3: FE chịu trách nhiệm context filter
AS trả all contexts → FE filter chỉ show contexts phù hợp với portal hiện tại
→ Context selection logic nằm ở FE, không phải AS → security concern

Ảnh hưởng đến ABAC:

// Token không có scope:operator rõ ràng — chỉ có activeScope trong custom claim
// PolicyProvider phải đọc custom claim, không phải standard OAuth2 scope
// Nếu custom claim bị thiếu → load sai PolicySet, lỗi silent

Token claims (shared client):
scope: "openid profile"     ← standard scopes, không carry portal context
activeScope: "operator"     ← custom claim — phụ thuộc vào implementation AS
orgId: "building-x"

Với separate client, scope:operator là standard OAuth2 scope — các library đều đọc được. Với shared   
client, activeScope là custom claim — phải tự handle ở mọi nơi.

  ---
So sánh với các hệ thống tương tự

Keycloak:
Pattern chuẩn: một Client per application (portal)
- Admin Console: client "admin-console"
- User portal: client "account-console"
- Custom apps: mỗi app = 1 client

Client Scopes gắn với client cụ thể → scope isolation tự nhiên
Realm-level separation cho multi-tenant nặng: mỗi tenant một realm
→ Powerful nhưng operational overhead cao (N realms × M configs)

Auth0:
Pattern chuẩn: một Application per portal (1-to-1 với OAuth2 client)
- Separate client_id cho Admin, Operator, Tenant, Resident
- Custom Actions (Rules) chạy per application → có thể add portal-specific claims
- Organizations feature: context switching tích hợp sẵn cho multi-tenant
→ Organization = workspace, user là member với roles trong org
→ Token tự động carry org_id + roles trong org
→ Gần với Solution 4 (Invitation/Workspace model)

Okta:
Pattern chuẩn: Applications riêng per portal
- Sign-on Policy per application: admin portal yêu cầu MFA, resident portal không
- Okta "Orgs" cho multi-tenant tương tự Auth0 Organizations

Azure AD B2C / Entra External ID:
App registration per portal, kết hợp với User Flows/Custom Policies
- Separate app = separate token lifetime policy
- Cross-portal: user phải re-authenticate hoặc dùng silent auth (SSO cookie)

Nhận xét chung từ industry:

▎ Separate client per portal là pattern chuẩn. Không có vendor nào khuyến khích shared client cho     
▎ multiple portals với different security requirements.

Lý do nhất quán: independent security policy per portal (token TTL, MFA requirement, scope isolation)
không thể đạt được với shared client.

  ---
Một middle-ground option — Scope-based differentiation

Thay vì tách client hoặc gộp hết, dùng OAuth2 scope như portal gate:

Vẫn một client (hoặc ít client hơn), nhưng scope carry portal context:

/authorize?client_id=apartcom-web&scope=openid profile scope:operator

AS logic:
- User request scope:operator → AS check user có OPERATOR context không
- Nếu có nhiều OPERATOR contexts → show context selector (chỉ OPERATOR)
- Nếu không có → error: insufficient_scope
- Token: { scope: "scope:operator", orgId: "...", roles: [...] }

Cách này:
- Không cần tách client hoàn toàn
- Portal differentiation qua scope — standard OAuth2 mechanism
- AS có thể enforce: client A chỉ được request scope:operator và scope:tenant, không được request     
  scope:admin
- Token scope = portal context → downstream đọc được

Thực ra đây là Approach A simplified — vẫn cần scope mapping per portal nhưng có thể share client nếu
security policy cho phép (cùng TTL, cùng MFA requirement).

  ---
Đánh giá

┌───────────────┬──────────────────────────┬─────────────────────────┬──────────────────────────┐     
│               │     Separate Clients     │      Shared Client      │       Scope-based        │     
├───────────────┼──────────────────────────┼─────────────────────────┼──────────────────────────┤     
│ Generic       │ 5/5 — industry standard, │ 2/5 — workaround nhiều, │ 4/5 — flexible, cần AS   │     
│               │  mọi IdP support         │  không scale            │ custom logic             │     
├───────────────┼──────────────────────────┼─────────────────────────┼──────────────────────────┤     
│ ABAC          │ Thấp — scope trong token │ Cao — custom claim,     │ Trung bình — scope chuẩn │     
│ complexity    │  = PolicySet selector    │ ambiguity risk          │  nhưng cần AS logic      │     
├───────────────┼──────────────────────────┼─────────────────────────┼──────────────────────────┤     
│ UI/Nav        │ Thấp — context list      │ Cao — FE phải filter    │ Trung bình               │     
│ complexity    │ filtered by AS           │ contexts, mismatch risk │                          │     
├───────────────┼──────────────────────────┼─────────────────────────┼──────────────────────────┤     
│ Operational   │ Trung bình — N clients   │ Thấp ban đầu, cao dài   │ Trung bình               │     
│               │ cần manage               │ hạn                     │                          │     
├───────────────┼──────────────────────────┼─────────────────────────┼──────────────────────────┤     
│ Security      │ Cao — independent TTL,   │ Thấp — blast radius lớn │ Trung bình-cao           │     
│ isolation     │ scope, policy            │  hơn                    │                          │     
└───────────────┴──────────────────────────┴─────────────────────────┴──────────────────────────┘

Khuyến nghị: Separate client per portal nhóm (không nhất thiết per portal URL). Nhóm hợp lý:

client: platform-admin     ← Admin Portal (TTL ngắn, MFA required)
client: building-ops       ← Operator Portal + Tenant Portal (security policy tương đồng)
client: end-user           ← Resident Portal + Tenant Employee (low privilege, long TTL)

Ba clients thay vì bốn — giảm operational overhead, vẫn giữ được security isolation theo nhóm risk.  


Cross-Portal — Phân loại và Ví dụ Cụ thể

Cross-portal thực ra là 3 vấn đề khác nhau bị gọi chung một tên. Phân biệt trước để không giải nhầm
bài toán.
  ---                                                                                                   
Loại 1 — Một người, nhiều portal (Identity crosses portals)
Ví dụ: Anh Minh vừa là BQL Manager của Tòa nhà X, vừa là cư dân căn hộ 10B của chính tòa nhà đó.

8:00 sáng: Anh Minh mở Operator Portal xử lý work order
14:30 chiều: Anh Minh muốn xem hóa đơn dịch vụ tháng này của căn hộ mình
→ Cần vào Resident Portal

Với Separate Clients:

Operator Portal session:
Token: { client_id: operator-portal, scope: operator,
orgId: building-x, roles: [BQL_MANAGER] }

→ Anh Minh mở tab mới: resident.apartcom.vn
→ Browser redirect đến AS: /authorize?client_id=resident-portal
→ AS thấy SSO session còn hiệu lực (cookie) → không yêu cầu login lại
→ AS filter contexts theo client_id=resident-portal → chỉ show RESIDENT contexts
→ Anh Minh thấy: "Căn hộ 10B - Cư dân" → click chọn
→ Token mới: { client_id: resident-portal, scope: resident,
unitId: unit-10b, roles: [RESIDENT] }

Kết quả: 2 tab, 2 token độc lập, không ảnh hưởng nhau

                  Tab 1 (Operator Portal)       Tab 2 (Resident Portal)
Token:          scope=operator               scope=resident
orgId=building-x             unitId=unit-10b
roles=[BQL_MANAGER]          roles=[RESIDENT]
TTL=8h                       TTL=12h

ABAC:           Load OPERATOR PolicySet      Load RESIDENT PolicySet
Navigate:       Work orders, staff, finance  Hóa đơn, tiện ích, đặt chỗ

No contamination. AS enforces portal boundary tại thời điểm cấp token.

Với Shared Client:

Login vào apartcom-web → AS hiện context selector:
[x] Operator Portal - Building X (BQL Manager)
[ ] Resident Portal - Unit 10B

Anh Minh chọn Operator Portal → token với activeScope=OPERATOR
→ Muốn chuyển sang Resident: click "Switch Context" trong app
→ Re-request token với activeScope=RESIDENT

Vấn đề phát sinh:
- Context switcher ở đâu trong UI? Header dropdown?
- Khi switch, operator portal session có bị clear không?
- Nếu 2 tab: cả 2 tab dùng cùng shared session storage?
→ Tab 1 switch context → Tab 2 bị logout/nhầm context

Kết luận Loại 1: Separate clients xử lý cleanly hơn — browser tabs tự nhiên là isolation boundary.    
Shared client phải tự xây isolation logic.

  ---
Loại 2 — Dữ liệu / Workflow crosses portals (Data crosses portals)

Ví dụ: Tenant Admin của Công ty Z tạo yêu cầu sửa chữa máy lạnh văn phòng.

Actors liên quan:
Tenant Admin   → Tenant Portal    (tạo yêu cầu)
BQL Technical  → Operator Portal  (nhận và xử lý)
Nhà thầu       → External Portal  (cập nhật tiến độ)
Tenant Admin   → Tenant Portal    (theo dõi trạng thái)

[Tenant Portal] Tenant Admin tạo request:
MaintenanceRequest {
id: "REQ-001",
tenantId: "company-z",
type: "HVAC",
location: "Floor 5, Zone B",
status: PENDING
}

[Operator Portal] BQL Technical nhận:
Cùng REQ-001, hiển thị như WorkOrder {
id: "WO-001",
linkedRequestId: "REQ-001",
assignedTo: "tech-nguyen",
status: IN_PROGRESS
}

[External Portal] Nhà thầu cập nhật:
WorkOrderUpdate {
workOrderId: "WO-001",
progress: 80%,
note: "Đã thay gas, đang test"
}

Đây KHÔNG phải vấn đề portal architecture. Đây là vấn đề data model và ABAC policy.

Câu hỏi thực sự: Ai được đọc/ghi REQ-001?

ABAC Policy cho REQ-001:
Tenant Admin của company-z   → PERMIT (owner)
BQL Technical của building-x → PERMIT (operator has visibility into tenant requests)
Nhà thầu assigned WO-001     → PERMIT (scoped to this work order only)
Cư dân tầng 5                → DENY (không liên quan đến họ)
BQL của building khác        → DENY (wrong org)

Portal chỉ là cửa vào. Dữ liệu nằm ở một nơi, access control quyết định ai thấy gì — không phụ thuộc  
vào portal architecture là separate hay shared.

Kết luận Loại 2: Separate vs Shared client không ảnh hưởng. Đây là bài toán của ABAC instance-level   
authorization (Vấn đề 3 trong analysis doc).

  ---
Loại 3 — Admin oversight crosses portals (Privilege crosses portals)

Ví dụ: Super Admin nhận ticket support: "BQL Manager của Tòa nhà X báo menu Finance bị ẩn dù có       
quyền."

Admin cần nhìn thấy đúng những gì BQL Manager thấy để debug.

Với Separate Clients — Vấn đề rõ nhất:

Admin đang ở Admin Portal:
Token: { client_id: admin-portal, scope: system, roles: [SUPER_ADMIN] }

Muốn xem Operator Portal như BQL Manager:
→ Không thể dùng admin-portal token vào operator.apartcom.vn
→ Token scope=system, Operator Portal expect scope=operator → mismatch
→ Phải re-authenticate với client_id=operator-portal
→ Nhưng Admin không có BQL_MANAGER role → AS không cấp operator context
→ Dead end

Cách giải quyết đúng — Impersonation Feature (tách biệt hoàn toàn):

Admin Portal → UI: "Debug: Simulate as user" → chọn user "bql-manager-b"
→ Admin Service issue temporary support token:
{
sub: "bql-manager-b",           ← identity của người được simulate
scope: "operator",
orgId: "building-x",
roles: ["BQL_MANAGER"],
impersonatedBy: "super-admin-a", ← audit trail
exp: now + 30min                ← short-lived
}
→ Admin mở operator.apartcom.vn với token này
→ Thấy đúng những gì BQL Manager b thấy
→ Audit log: "super-admin-a impersonated bql-manager-b at 15:22 for 12 minutes"

Cơ chế này hoạt động giống nhau với cả separate và shared client — impersonation không phụ thuộc vào  
client architecture. Separate client thậm chí dễ hơn vì admin explicitly mở operator portal với token
riêng.

  ---
Tổng hợp — Cross-portal là 3 bài toán khác nhau

┌──────────────────────────────────────────────────────────────────┐
│ Loại 1: Một người, nhiều portal                                   │
│   Bài toán: Identity / session isolation                         │
│   Giải pháp: Context switching + separate client                 │
│   Separate client: ✓ Clean, browser tabs = natural isolation     │
│   Shared client:   ✗ Cần tự xây isolation, tab conflict risk     │
├──────────────────────────────────────────────────────────────────┤
│ Loại 2: Data/workflow crosses portals                             │
│   Bài toán: Instance-level ABAC (ai được đọc object nào)         │
│   Giải pháp: Data model + ABAC policy (object attributes)        │
│   Separate client: = Không ảnh hưởng                            │
│   Shared client:   = Không ảnh hưởng                            │
├──────────────────────────────────────────────────────────────────┤
│ Loại 3: Admin oversight / impersonation                           │
│   Bài toán: Privilege delegation có audit trail                  │
│   Giải pháp: Impersonation feature — separate từ auth flow       │
│   Separate client: ✓ Explicit, dễ audit                         │
│   Shared client:   ~ Được, nhưng boundary kém rõ hơn            │
└──────────────────────────────────────────────────────────────────┘

Điểm mấu chốt: Khi người ta nói "cross-portal phức tạp", hầu hết lo ngại thực ra là Loại 2 — nhưng    
Loại 2 không phụ thuộc vào portal architecture. Separate client giải quyết tốt Loại 1 và Loại 3. Loại
2 là bài toán riêng hoàn toàn.

Tại sao Loại 3 khó xảy ra trong thực tế với Apartcom:                                                  
Ví dụ trong doc — "menu Finance bị ẩn dù có quyền" — thực chất là một ABAC evaluation bug. Kỹ thuật    
viên debug cái này bằng cách:
- Xem server-side log của policy evaluation
- Query DB kiểm tra roles/permissions của user đó
- Check UIElement registry xem element nào bị evaluate sai

Không cần nhìn thấy UI của user mới debug được. Bug nằm ở logic, không phải ở state chỉ user đó có.

Những trường hợp Impersonation thực sự có giá trị:

Impersonation justify khi có cả 3 điều kiện cùng lúc:
1. Scale đủ lớn — không thể tạo test account cho từng case (Stripe có hàng triệu merchant, không thể
   tạo account giả từng người)
2. State không reproducible — user có data history, config, workflow state mà test account không có
3. Support team không phải developer — người cần debug là support agent, không có quyền truy cập DB hay
   logs

Apartcom hiện tại: technical team là developer, số lượng user nhỏ, bug thường nằm ở code logic → không
thỏa cả 3.

Phán đoán:

Loại 3 trong doc được viết để đủ bộ phân tích — nó là scenario hợp lệ về mặt lý thuyết, nhưng với giai
đoạn hiện tại của Apartcom nó là over-engineering rõ ràng. Impersonation còn đi kèm chi phí: security
surface mở rộng, audit log phải đúng 100%, legal concern về privacy.

Đề xuất thực tế:

Bỏ Loại 3 khỏi scope hiện tại. Nếu sau này scale lên mức SaaS thực sự (support team riêng, hàng nghìn
tenant), mới xem xét. Feature này độc lập hoàn toàn với auth architecture nên thêm sau không ảnh hưởng
gì.

Tóm lại: phân tích trong doc đúng về mặt kiến trúc, nhưng bạn đúng về mặt thực tế — nó không cần thiết
ở giai đoạn này.
````````````

