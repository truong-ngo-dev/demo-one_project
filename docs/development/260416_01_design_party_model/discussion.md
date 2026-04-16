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
