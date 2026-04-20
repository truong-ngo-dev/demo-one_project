# Discussion: Party Model Integration với Multi-Portal Architecture

## Context

Đã có:
- Draft Party Model + Fixed Asset design (party_model_design_01.md, party_model_fixed_asset.md)
- Multi-Portal Architecture design (02_multi_portal_architecture.md): ADMIN / OPERATOR / TENANT / RESIDENT portals, RoleContext { scope, orgId, roles }, two-phase auth

Vấn đề cần giải quyết:
1. orgId trong RoleContext hiện tại là opaque — cần map rõ sang Party/FixedAsset domain
2. Admin (SUPER_ADMIN) cần làm gì từ đầu (bootstrap sequence)
3. User có multiple contexts → UX context selector trông như thế nào

## Log

[CONSTRAINT] orgId semantic per scope — cần cụ thể hoá
- What: orgId trong RoleContext phải map sang domain entity cụ thể theo từng scope
  ADMIN    → null (platform-wide)
  OPERATOR → FixedAsset.id (type=BUILDING)
  TENANT   → Party.id (type=Organization, subtype=TENANT)
  RESIDENT → FixedAsset.id (type=RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE)
- Why: Context selector cần resolve display name; lifecycle (OccupancyAgreement terminate → revoke RoleContext) cần biết entity type
- Affects: RoleContext model, GET /auth/contexts response, admin service validation

[DECISION] OccupancyAgreement.partyId là polymorphic Party reference
- What: partyId trỏ vào Party base type — concrete type là Person / HOUSEHOLD / Organization
  Person → cá nhân thuê/sở hữu → 1 người có RESIDENT access
  HOUSEHOLD → hộ gia đình → toàn bộ members có RESIDENT access
  Organization → công ty thuê commercial → TENANT context, không phải RESIDENT
- Why: Hybrid tự nhiên của Party Model. Không cần AuthorizedResident list trên Agreement. Gate logic thống nhất 1 điều kiện.
- Affects: OccupancyAgreement aggregate, RoleContext gate logic, BQL workflow (chọn party type khi tạo agreement)

[DECISION] HOUSEHOLD là Party subtype (informal group)
- What: Party type mới: HOUSEHOLD. Có headPerson (đại diện pháp lý), members qua PartyRelationship(MEMBER_OF).
  Tái sử dụng được: chuyển nhà → agreement mới, cùng HOUSEHOLD party
- Why: Mirrors hộ khẩu Việt Nam. Membership management là Party operation, tách khỏi Agreement.
- Affects: Party domain model, PartyRelationship types

[DECISION] RESIDENT RoleContext gate — unified rule
- What: Person P có thể có RoleContext { RESIDENT, unit-X } khi:
  ∃ OccupancyAgreement ACTIVE trên unit-X WHERE
    agreement.partyId = P  (cá nhân)
    OR agreement.partyId là HOUSEHOLD mà P là member (hộ gia đình)
- Why: Bao phủ cả hai case bằng 1 điều kiện. Party polymorphism xử lý sự phân kỳ.
- Affects: RoleContext creation validation, BQL assign RESIDENT workflow

[OPEN] OPERATOR orgId = Building FixedAsset ID hay BQL Organization Party ID?
- Option A (Building FixedAsset ID): Align với per-building scope, physical dimension rõ ràng, consistent với RESIDENT (unit ID)
- Option B (BQL Organization Party ID): Align với Employment model (Person employed-by BQL Org), "org" dimension tường minh
- Tension: Employment relationship trong Party model trỏ Person → BQL Org, nhưng portal scope là per-building — hai concept khác nhau
- Leaning: Option A (Building ID) vì scope unit là Building, không phải Management Company. BQL Org là implementation detail của ai vận hành building đó.

[CONSTRAINT] Admin bootstrap sequence — phân tách rõ ADMIN vs OPERATOR responsibilities
- What: Chỉ có 3 thứ SUPER_ADMIN cần làm khi onboard building mới:
  (1) Tạo Building root (FixedAsset type=BUILDING)
  (2) Tạo BQL Organization (Party) gắn với building
  (3) Tạo User + assign RoleContext { OPERATOR, building-x } → bootstrap BQL_MANAGER đầu tiên
  Mọi thứ còn lại (floor/unit setup, tenant, resident) → BQL_MANAGER làm trong OPERATOR portal
- Why: Separation of concerns — Admin cấp phép, Operator vận hành. Admin không biết cấu trúc nội bộ của từng tòa nhà.
- Affects: ADMIN portal use cases, OPERATOR portal use cases

[OPEN] Context selector display — cần enrich GET /auth/contexts response
- What: Response hiện tại { scope, orgId, roles } không đủ để render selector có nghĩa
  User thấy "chung cư 103" hay "unit-101"?
- Options:
  A) Admin service resolve displayName khi trả contexts (internal lookup trong phase 1 khi cùng service)
  B) Snapshot displayName vào RoleContext lúc assign
  C) FE tự fetch tên sau khi nhận contexts
- Leaning: Phase 1 → Option A (cùng service → internal lookup). Phase 2 (tách service) → cần event snapshot.

[CONSTRAINT] OccupancyAgreement là điều kiện tiên quyết của RESIDENT/TENANT RoleContext
- What: RoleContext { scope=RESIDENT, orgId=unit-x } chỉ được tạo khi có OccupancyAgreement ACTIVE cho unit đó
  Khi OccupancyAgreement terminate → RoleContext bị revoke
- Why: Party model là business truth, RoleContext là derived. Không được tạo RESIDENT context cho unit mà không có hợp đồng chiếm dụng.
- Affects: RoleContext creation validation, event design (OccupancyAgreementTerminated → RoleContextRevoked)

[OPEN] Ai trigger revoke RoleContext khi OccupancyAgreement terminate?
- Option A: Domain Event — OccupancyAgreementTerminated → async handler revoke RoleContext
- Option B: Manual step của BQL (terminate agreement, then manually revoke)
- Option C: Sync — OccupancyAgreement terminate operation cũng gọi RoleContext service trực tiếp
- Tension: Phase 1 all-in-one service → Option C dễ. Phase 2 → cần event.

[DECISION] orgId = scope dimension, không phải identity — rationale confirmed
- What: orgId trong RoleContext đại diện cho "ranh giới dữ liệu" (scope), không phải "ai bạn là" (identity)
  RESIDENT → Unit.id vì scope là physical space được chiếm dụng
  TENANT   → Party.id (Org) vì 1 Org có thể thuê nhiều unit → không thể gắn vào 1 unit cụ thể
  OPERATOR → Building.id vì scope là per-building, không phải per-management-company
- Why: orgId phải là entity có cardinality-1 với user trong portal đó. TENANT Org là exception vì nhiều unit dưới 1 Org → Party.id là natural scope anchor.
- Affects: RoleContext model, context selector display logic

[DECISION] OccupancyAgreement và EmploymentRelationship là 2 model riêng biệt
- What: Không unify thỏa thuận BQL-resident, BQL-tenant, BQL-staff thành 1 Agreement supertype
  OccupancyAgreement → có FixedAsset làm trung tâm, lifecycle gắn với unit
  EmploymentRelationship (PartyRelationship EMPLOYED_BY) → không có FixedAsset, lifecycle gắn với Org
- Why: Domain logic khác nhau: OccupancyAgreement quản lý "ai ở đâu", Employment quản lý "ai làm gì". Unify tạo model chung mà mỗi subtype dùng tập field khác nhau.
- Affects: Party domain model, agreement lifecycle design

[DECISION] OPERATOR portal — 5 nhóm nghiệp vụ cốt lõi
- What: Toàn bộ operation sau bootstrap do OPERATOR thực hiện:
  (1) Cấu trúc tòa nhà: Floor, Unit (RESIDENTIAL_UNIT, COMMERCIAL_SPACE)
  (2) Nhân sự BQL: Person → EMPLOYED_BY → BQL Org, assign OPERATOR RoleContext
  (3) Tenant: Tạo Tenant Org (Party), OccupancyAgreement commercial, assign TENANT RoleContext
  (4) Resident: Tạo Person/HOUSEHOLD, OccupancyAgreement residential, assign RESIDENT RoleContext
  (5) Vòng đời hợp đồng: Terminate Agreement → revoke RoleContext
- Why: Separation of concerns với ADMIN. ADMIN chỉ bootstrap (Building + BQL Org + BQL_MANAGER đầu tiên).
- Affects: OPERATOR portal use cases, use case ordering (1→2→3→4 là dependency chain)

[DECISION] Silverston Party Model — selective adoption ~40-50%
- What: Không implement full Silverston. Các concept được giữ/bỏ:
  GIỮ: Party/Person/Organization, PartyRelationship (EMPLOYED_BY, MEMBER_OF), PartyIdentification (CCCD, MST)
  CÂN NHẮC: AgreementRole (nếu BQL đóng nhiều vai trò pháp lý)
  BỎ: AgreementItem/AgreementTerm (misfit với occupancy), ContactMechanism normalize (over-engineering), PartyClassification (no use case), PartyRole as entity (RoleContext đã handle)
- Why: Silverston thiết kế cho enterprise-wide party management. Ở scale tòa nhà đơn/vừa, full implementation tạo join complexity không có ROI. PartyRole as entity double-model với RoleContext ở auth layer.
- Affects: Party domain model scope, data model decisions

[DECISION] PartyRole thay thế bằng enum trên relationship/agreement
- What: Không có PartyRole as entity hay CRUD. Thay bằng:
  PartyRelationshipType enum: MEMBER_OF, EMPLOYED_BY
  PartyRoleType enum: MEMBER, HEAD, EMPLOYEE, EMPLOYER, LESSOR, LESSEE
  Dùng fromRole/toRole trên party_relationship, partyRole trên agreement
- Why: Role types cố định, ít, không cần data-driven. Enum là type-safe và đơn giản hơn. PartyRole CRUD không có use case thực tế — admin CRUD Role entity trong RoleContext, không phải PartyRole.
- Affects: party_relationship table, agreement table, Party domain model

[DECISION] FixedAsset là hierarchy riêng, không phải Party subtype
- What: Party = entity có legal identity (có thể ký kết, chịu trách nhiệm pháp lý). FixedAsset = vật thể vật lý, không có legal identity. Hai hierarchy song song, không lồng nhau.
  Party: Person, Organization, Household
  FixedAsset: Building → Floor → Unit (self-referencing)
- Why: Gộp FixedAsset vào Party làm mất semantic "ai" vs "cái gì". Agreement nối 2 chiều: Party ký kết VỀ Asset — cần tách biệt để model rõ ràng.
- Affects: ERD, service boundary, Agreement model (partyId + unitId là 2 FK khác domain)

[DECISION] Household KHÔNG gộp vào Organization
- What: Giữ 3 subtype riêng: Person, Organization, Household
- Why: Household là informal group (không có legal identity, không đăng ký pháp nhân). Gộp vào Organization tạo nullable fields, lẫn lộn MEMBER_OF với EMPLOYED_BY semantic, query "tất cả pháp nhân" phải filter bỏ Household.
- Affects: Party domain model, party_relationship types

[DECISION] Employment chỉ có ý nghĩa với BQL Org — các relationship khác dùng MEMBER_OF
- What: 
  Person → BQL Org: EMPLOYED_BY + employment subtype table (HR detail: position, department)
  Person → Household: MEMBER_OF (không có subtype table)
  Person → Tenant Org: MEMBER_OF (không có subtype table — chỉ track "ai được phép hoạt động trong tòa")
- Why: Hệ thống chỉ quản lý HR của BQL. Tenant Org dùng hệ thống HR riêng — hệ thống này chỉ cần biết "ai đại diện/được ủy quyền". MEMBER_OF tái sử dụng được cho cả Household và Tenant Org membership.
- Affects: party_relationship types, employment subtype scope, TENANT portal use cases

[DECISION] Service boundary — 3 service riêng biệt
- What: Tách thành 3 service theo domain concern:
  admin-service    → IAM thuần: User, Role, RoleContext, ABAC
  party-service    → Party, PartyRelationship, Employment, PartyIdentification
  property-service → FixedAsset, Agreement, OccupancyAgreement
- Why: Admin service hiện tại đã nặng (User + Role + ABAC). Party và FixedAsset là business domain concern, không phải IAM concern. Admin chỉ nên biết "who you are" và "what you can do", không biết "who lives where".
- Affects: Service decomposition, cross-service event design, deployment

[DECISION] Cross-service coordination qua event, không phải direct call
- What: 2 flow chính dùng event:
  (1) Bootstrap: property-service emit BuildingCreated → admin-service cache reference → validate khi assign RoleContext
  (2) Lifecycle: property-service emit OccupancyAgreementActivated → admin-service tạo RoleContext cho party members
                 property-service emit OccupancyAgreementTerminated → admin-service revoke RoleContext WHERE orgId=unitId
- Why: Direct call tạo tight coupling — nếu downstream service down thì upstream fail theo. Event cho phép async processing, audit trail tự nhiên, thêm consumer không cần sửa producer.
- Affects: Event contract design, admin-service event handlers, property-service domain events

[DECISION] User.party_id nullable — rule enforcement theo scope
- What: user { party_id nullable, ref → party-service }
  party_id = null  → non-human hoặc platform-level user (SUPER_ADMIN, system/service account)
  party_id != null → human user có physical presence trong hệ thống
  Rule: scope != ADMIN → party_id bắt buộc (OPERATOR/TENANT/RESIDENT đều cần Party context)
- Why: Mọi portal ngoài ADMIN đều cần Party để hoạt động (RoleContext gate, membership resolution). SUPER_ADMIN và service account không đại diện cho person thực tế.
- Affects: User domain model, User creation validation, RoleContext assignment gate

---

## Compact từ các design docs

[DECISION] Actor → Party type mapping — đầy đủ
- What: Mọi "người" → Person (Party). Mọi "tổ chức" → Organization (Party).
  Person: IT Admin, BQL_MANAGER, BQL_STAFF, TENANT_ADMIN, TENANT_EMPLOYEE, Unit Owner, Resident, Visitor, Contractor employee
  Organization: BQL company (type=BQL), Tenant company (type=TENANT), Contractor company (type=VENDOR)
- Why: Party Model là single entry point cho mọi entity có legal/social identity. Phân loại bằng OrgType enum, không phải bằng class riêng.
- Affects: Party domain model, OrgType enum

[DECISION] PartyRelationship types — đầy đủ
- What:
  EMPLOYED_BY  → Person → BQL Org (có Employment subtype với HR detail)
  MEMBER_OF    → Person → Household, Person → Tenant Org (không có subtype)
  LANDLORD_TENANT → Organization(BQL) ↔ Organization(Tenant)
  CLIENT_VENDOR   → Organization(BQL) ↔ Organization(Contractor)
  VISITOR_OF      → Person(Visitor) ↔ Person(Host) hoặc Organization(Tenant), period = time window
- Why: Các loại quan hệ này cover toàn bộ interaction cần track trong BMS. VISITOR_OF là time-bound (period có from/to là datetime).
- Affects: PartyRelationshipType enum, PartyRelationship aggregate

[DECISION] FixedAsset type catalogue — cố định
- What: FixedAssetType là catalogue với các giá trị:
  BUILDING, FLOOR, RESIDENTIAL_UNIT, COMMERCIAL_SPACE, COMMON_AREA, FACILITY, MEETING_ROOM, PARKING_SLOT, EQUIPMENT
  Hierarchy: BUILDING → FLOOR → (RESIDENTIAL_UNIT | COMMERCIAL_SPACE | COMMON_AREA | FACILITY | MEETING_ROOM | PARKING_SLOT)
  EQUIPMENT gắn vào bất kỳ node nào (thang máy thuộc floor, máy phát thuộc building)
- Why: Type là catalogue → thêm type mới không cần thay đổi schema. Phân loại này cover đủ các không gian cần quản lý trong BMS.
- Affects: FixedAssetType catalogue, FixedAsset aggregate

[DECISION] FixedAsset hierarchy dùng materialized path
- What: FixedAsset lưu parentId (adjacency list) + path string (materialized path)
  path format: /bldg-A/flr-1/u-101
  Query "tất cả unit thuộc tầng 1": WHERE path LIKE '/bldg-A/flr-1/%'
  Query "tất cả residential unit thuộc building": WHERE path LIKE '/bldg-A/%' AND type = RESIDENTIAL_UNIT
- Why: Adjacency list đơn giản để write, materialized path tối ưu query cây nhiều cấp. Self-referencing FK duy nhất.
- Affects: fixed_asset table schema, FixedAsset aggregate, hierarchy traversal queries

[DECISION] OccupancyAgreement — structure và invariants
- What: OccupancyAgreement { partyId → Person/Org, assetId → FixedAsset, agreementType → OWNERSHIP/LEASE,
  status → PENDING/ACTIVE/TERMINATED/EXPIRED, period → {startDate, endDate}, contractRef }
  Invariants:
  [I1] Max 1 ACTIVE OWNERSHIP per FixedAsset
  [I2] Max 1 ACTIVE LEASE per FixedAsset tại cùng thời điểm
  [I3] OWNERSHIP và LEASE có thể đồng thời tồn tại trên cùng 1 unit (chủ cho thuê lại)
  [I4] LEASE chỉ được tạo trên RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE
  [I5] OWNERSHIP chỉ được tạo trên RESIDENTIAL_UNIT (commercial space không bán riêng lẻ)
- Why: Invariants bảo vệ tính nhất quán của occupancy model. I3 là quy tắc quan trọng — owner và lessee cùng tồn tại là hợp lệ.
- Affects: OccupancyAgreement aggregate, domain invariant enforcement

[CONSTRAINT] Gap domains — nằm ngoài 3 service chính
- What: Các domain cần design riêng, KHÔNG thuộc party/property/admin service:
  Access Control  → AccessCard lifecycle riêng, gắn Person ↔ Zones, không phải Party relationship
  Visitor         → Person transient (VisitorLog: tên, CMND, host, time window) — không cần full Party record
  Booking         → Reservation của Facility theo time slot — semantics khác Work Effort
  Maintenance     → Work Order gắn với Equipment (Fixed Asset type=EQUIPMENT), gần Ch.6 Work Effort
- Why: Các domain này có lifecycle và logic riêng biệt, không fit vào party/property model. Build chung sẽ tạo coupling không cần thiết.
- Affects: Service roadmap — các domain này là phase sau

[DECISION] Tenant support scope — Tier 1+2, defer Tier 3
- What: 3 tầng support tenant:
  Tier 1 (bắt buộc): access card, facility booking, billing, visitor registration — lý do tenant có tài khoản
  Tier 2 (value-added): tenant employee directory, sub-roles, timesheet/attendance tích hợp access control
  Tier 3 (defer): Dynamic CRM, custom workflow, custom fields, ABAC self-service
- Why: Không có BMS nào build full CRM nội bộ cho tenant. Apartcom định vị là "building-context tools", không phải general-purpose CRM. Tier 3 out of scope cho BMS product.
- Affects: TENANT portal use cases, product roadmap

[DECISION] Tenant sub-role — Option 2: platform-defined capabilities, tenant assigns
- What: TENANT_ADMIN không tự viết ABAC policy. Platform define cố định sub-role capabilities.
  TENANT_ADMIN tạo: TENANT_DEPT_HEAD, TENANT_HR, TENANT_FINANCE (nếu cần)
  Platform define capability của mỗi sub-role (fixed set)
  TENANT_ADMIN chỉ assign sub-role cho người
- Why: Tenant self-service ABAC (Option 3) yêu cầu SpEL sandbox, scoped ABAC console, rollback mechanism — complexity rất cao, không ROI ở scale hiện tại.
- Affects: RoleContext extension, Role registry nếu sub-role là dynamic

[CONSTRAINT] BQL cross-read boundary — cần enforce ở 2 tầng
- What: BQL được đọc từ tenant module:
  ĐƯỢC: danh sách nhân viên (tên, phòng), số lượng có mặt, access card active, phương tiện đăng ký, hóa đơn
  KHÔNG ĐƯỢC: mức lương, hồ sơ nhân sự chi tiết, email/chat nội bộ, hiệu suất, tài liệu nội bộ
  Enforcement: DB-level isolation + ABAC explicit DENY (không chỉ ABAC)
- Why: Cross-tenant data leakage là security breach. ABAC alone không đủ — nếu ABAC rule sai thì không có backstop.
- Affects: TENANT_PRIVATE data isolation design, ABAC DENY rules, cần quyết định DB isolation strategy trước khi code

[DECISION] Silverston technical implementation choices
- What: 4 điểm khác biệt kỹ thuật so với Silverston nguyên bản:
  (1) Subtype pattern: Composition + Shared ID thay vì Inheritance
      Party ← shares ID → Person / Organization / Household
      Lý do: microservices-friendly, mỗi subtype là aggregate độc lập, không join inheritance chain
  (2) Contact Mechanism: EAV (PartyAttribute) thay vì các bảng riêng (Phone, Email, Address)
      Lý do: linh hoạt dynamic attributes, không cần schema change khi thêm contact type mới
  (3) Organization Hierarchy: Materialized Path thay vì adjacency list đơn giản
      Lý do: tối ưu query cây nhiều cấp (subtree, path lookup) không cần recursive CTE
  (4) Employment: Aggregate Root riêng thay vì subtype của Relationship
      Lý do: lifecycle riêng (ACTIVE/TERMINATED độc lập với relationship), PositionAssignment history
- Why: Các lựa chọn này ưu tiên query performance và microservices independence hơn strict Silverston conformance. Trade-off chấp nhận được ở scale tòa nhà đơn/vừa.
- Affects: party table design, party_attribute table, org hierarchy query, employment aggregate

[OPEN] DB isolation strategy cho TENANT_PRIVATE data
- Option A: Separate schema per tenant (strong isolation, complex ops)
- Option B: Encrypted columns (flexible, overhead decrypt)
- Option C: ABAC only (simplest, no DB-level backstop)
- Tension: Toàn bộ cross-tenant query design phụ thuộc vào quyết định này. Cần resolve trước khi code tenant module.

---

## Analysis — Plan review và Employment aggregate

[CONSTRAINT] Plan gaps cần giải quyết trước khi code

Gap 1 — Position / PositionAssignment bị bỏ hoàn toàn:
  Plan hiện tại có EmploymentType nhưng không có Position (BQL_MANAGER, BQL_FINANCE, ...) và PositionAssignment.
  RoleContext assignment cần biết Person đang giữ chức vụ gì để assign đúng roles.
  Nếu không có Position, admin-service không có basis để derive RoleContext roles.

Gap 2 — Business rule #3 cần cross-aggregate data:
  "Employment chỉ tạo khi toParty.orgType = BQL" yêu cầu load Party khác trong cùng use case.
  Cần quyết định: validate trong domain service (application layer) hay guard tại API level?
  Domain không được gọi repository của aggregate khác → phải là application service validation.

Gap 3 — Cross-service event schema chưa có:
  EmploymentCreatedEvent / EmploymentTerminatedEvent được list nhưng không có payload schema.
  admin-service cần consume để assign/revoke RoleContext — đây là event quan trọng nhất trong flow.
  Cần define schema trước khi implement cả 2 phía.

Gap 4 — property-service plan chưa tồn tại:
  Discussion đã quyết định 3 service riêng biệt. party-service có plan rồi.
  property-service (FixedAsset + OccupancyAgreement) cần plan riêng — dependency chain: party-service phải có trước.

[DECISION] Employment — tách thành Aggregate Root riêng
- What: Employment KHÔNG phải Entity trong PartyRelationship. Là Aggregate Root riêng.
  PartyRelationship (AR): thin, chỉ track kết nối { fromPartyId, toPartyId, type, fromRole, toRole, period, status }
  Employment (AR riêng): { partyRelationshipId (FK), employeeId, orgId, employmentType, status, List<PositionAssignment> }
  Employment được tạo khi và chỉ khi có PartyRelationship(EMPLOYED_BY) tương ứng.
  Terminate employment = update Employment.status, KHÔNG xóa PartyRelationship (audit trail).
- Why: PartyRelationship là kết nối mỏng, stateless. Employment là nghiệp vụ nhân sự với lifecycle riêng (ACTIVE/TERMINATED),
  có PositionAssignment theo thời gian. Nhét Employment vào PartyRelationship tạo mâu thuẫn lifecycle:
  terminate employment ≠ xóa relationship, nhưng PartyRelationship.status sẽ không còn rõ nghĩa.
- Affects: party-service aggregate design, plan.md Phase 2 cần update, PositionAssignment cần thêm vào Employment AR

[DECISION] Position — không có catalogue riêng, dùng enum trên PositionAssignment
- What: Bỏ Position entity. Employment có List<PositionAssignment> để support 1 người kiêm nhiều chức vụ đồng thời.
  PositionAssignment { id, employmentId, position (BQLPosition enum), department (string), startDate, endDate }
  BQLPosition enum: MANAGER, DEPUTY_MANAGER, FINANCE, TECHNICAL, SECURITY, RECEPTIONIST, STAFF
- Why: BQL position là fixed set, ít, không cần CRUD catalogue. Enum type-safe, đủ để query khi cần
  ("ai đang giữ FINANCE position"). Mọi business logic query đã đi qua RoleContext role — Position chỉ
  là thông tin hiển thị + HR record. Position catalogue chỉ worth it nếu có headcount/vacancy management
  — không phải BMS concern. Tenant Org không cần Position/PositionAssignment — dùng MEMBER_OF,
  "chức vụ" trong building context handle bởi TenantSubRoleAssignment.
- Affects: party-service Employment AR, PositionAssignment entity, BQLPosition enum

---

## Portal scope và ADMIN cross-portal access

[DECISION] TENANT portal scope — building-context only, không phản ánh nghiệp vụ nội bộ của tenant
- What: TENANT portal chỉ chứa các nghiệp vụ mà tenant thực hiện TRONG QUAN HỆ VỚI TÒA NHÀ:
  IN SCOPE: access card nhân viên, đặt facility, thanh toán phí dịch vụ, đăng ký khách thăm, nhận thông báo từ BQL
  OUT OF SCOPE: HR nội bộ của tenant, CRM của tenant, workflow nội bộ, tài liệu nội bộ
  Ranh giới: nếu nghiệp vụ không liên quan đến building → không có trong TENANT portal
- Why: Apartcom là BMS, không phải ERP/CRM cho tenant. Tenant có hệ thống riêng cho nghiệp vụ nội bộ.
  Việc phản ánh nghiệp vụ nội bộ của tenant vào Apartcom tạo scope creep và coupling không cần thiết.
- Affects: TENANT portal use case list, TENANT_ADMIN capabilities, API design cho TENANT scope

[DECISION] TENANT permission model — 2 layer tách biệt, benchmark từ CRE platforms (Equiem, HqO)
- What: Tách rõ 2 layer authorization trong TENANT portal:
  Layer 1 — Portal access (BQL owns): RoleContext { scope=TENANT, orgId=companyZ, roles=[TENANT_EMPLOYEE] }
    BQL grant khi onboard tenant member. BQL revoke khi OccupancyAgreement terminate.
    TENANT_ADMIN không được modify layer này.
  Layer 2 — Feature access (TENANT_ADMIN owns): tenant_sub_role_assignment { userId, orgId, subRole, assignedBy, assignedAt }
    TENANT_ADMIN assign từ tập sub-roles platform define sẵn.
    TENANT_ADMIN không tạo được role type mới, không viết custom policy.
    Sub-roles: TENANT_MANAGER, TENANT_FINANCE, TENANT_HR (platform define, fixed set)
- Why: CRE platforms cùng loại (Equiem, HqO) đều follow pattern này: platform-controlled role types,
  org admin chỉ assign. Mix 2 layer vào cùng roles[] tạo conflict ownership — BQL và TENANT_ADMIN cùng write vào 1 field.
  Bảng riêng giữ clear audit trail: ai grant portal access (BQL), ai assign feature role (TENANT_ADMIN).
- Affects: admin-service schema (thêm tenant_sub_role_assignment table), token generation (merge 2 nguồn),
  ABAC rule cho TENANT_ADMIN (chỉ write tenant_sub_role_assignment WHERE orgId = subject.orgId)

[DECISION] TENANT_ADMIN capabilities — member management + sub-role assignment
- What: TENANT_ADMIN có 2 nhóm use case trong TENANT portal:
  (1) Member management: thêm/xóa member trong org của mình (Party operation → party-service)
  (2) Sub-role assignment: assign/revoke sub-role từ tập platform-defined cho từng member (admin-service)
  TENANT_ADMIN KHÔNG được: define custom policy, modify RoleContext layer 1, assign role ngoài tập TENANT sub-roles,
  tự định nghĩa policy về space (booking rule, access control) — platform/BQL own toàn bộ space policy.
- Why: Space authorization tác động lên tài sản vật lý của tòa nhà — BQL phải kiểm soát.
  Tenant self-service ABAC cho space yêu cầu SpEL sandbox, scoped ABAC console — complexity không có ROI.
  Sub-role assignment là đủ để TENANT_ADMIN tổ chức internal permissions mà không cần expose ABAC engine.
- Affects: TENANT portal use cases, TENANT_ADMIN permission set, party-service member API, admin-service sub-role API

[DECISION] SUPER_ADMIN cross-portal access — Phase 1: pure separation, Phase 2: impersonation
- What:
  Phase 1 (hiện tại — Option A): SUPER_ADMIN chỉ ở ADMIN portal.
    Muốn vào portal khác → tạo user riêng với party_id + RoleContext tương ứng.
    Token structure không thay đổi, không có bảng mới.
  Phase 2 (khi support use case thực sự cần — Option D): User impersonation.
    SUPER_ADMIN issue impersonation session → nhận token của target user có thêm claim impersonation{}.
    Thêm: bảng impersonation_session, impersonation_audit_log, audit middleware ở web-gateway.
- Migration path: zero-impact.
    Token cũ không có impersonation claim → bỏ qua, không breaking.
    Data cũ không bị touch — chỉ thêm bảng mới và optional token field.
    Downstream services không cần thay đổi — token vẫn trông như token bình thường (sub, partyId, roles của target user).
- Impersonation invariants (Phase 2):
    [I1] Không impersonate SUPER_ADMIN khác — chặn privilege escalation
    [I2] Token roles[] bị cap ở target user's roles — không được nhiều hơn user được impersonate
    [I3] Max 1 active session per SUPER_ADMIN tại 1 thời điểm
    [I4] Max duration 60 phút, không gia hạn — buộc conscious decision mỗi lần access
- Why: Option A clean và nhất quán với party_id rule. Option D (impersonation) là industry-standard pattern
  (Salesforce, Okta) — không tạo special-case trong service logic, chỉ audit layer cần biết.
  Option B (god mode) bị loại vì special-case lan rộng ra mọi service, scale rất tệ.
- Affects: Phase 1 — không có gì thêm. Phase 2 — admin-service (impersonation session API),
  web-gateway (audit middleware), token structure (optional impersonation claim).
