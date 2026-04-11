Fixed Asset Hierarchy & OccupancyAgreement — Detailed Design                                                                                                                                                    
---                                                                                                      1. Fixed Asset — Aggregate Design                                                                                                                                                                               
1.1 Core structure                                                                                        
FixedAsset (Aggregate Root)                                                                            
├── FixedAssetId                                      
├── name
├── FixedAssetTypeId      → catalogue (BUILDING / FLOOR / UNIT / FACILITY / ...)
├── statusId              → ACTIVE / INACTIVE / UNDER_MAINTENANCE
├── parentId              → nullable — reference đến FixedAsset cha
├── sequenceNo            → thứ tự trong cùng parent (tầng 1, 2, 3...)
└── List<FixedAssetAttribute>   → EAV cho data đặc thù theo type

1.2 FixedAssetType — Catalogue

BUILDING            → tòa nhà
FLOOR               → tầng
RESIDENTIAL_UNIT    → căn hộ (cho người ở)
COMMERCIAL_SPACE    → văn phòng / shop (cho doanh nghiệp thuê)
COMMON_AREA         → hành lang, sảnh, cầu thang — không assign
FACILITY            → tiện ích đặt chỗ được (gym, pool, BBQ area)
MEETING_ROOM        → phòng họp — subset của FACILITY
PARKING_SLOT        → chỗ đỗ xe cụ thể
EQUIPMENT           → thang máy, máy phát điện, PCCC — dùng cho maintenance

1.3 Hierarchy thực tế

Building A (BUILDING)
├── Floor 1 (FLOOR, seq=1)
│   ├── Unit 101 (RESIDENTIAL_UNIT)
│   ├── Unit 102 (RESIDENTIAL_UNIT)
│   └── Office 1A (COMMERCIAL_SPACE)
├── Floor 2 (FLOOR, seq=2)
│   └── ...
├── Common (FLOOR, seq=0 — tầng trệt / khu chung)
│   ├── Lobby (COMMON_AREA)
│   ├── Meeting Room A (MEETING_ROOM)
│   ├── Gym (FACILITY)
│   └── Parking Zone B1 (FLOOR — có thể nest thêm)
│       ├── Slot B1-01 (PARKING_SLOT)
│       └── Slot B1-02 (PARKING_SLOT)
└── Equipment
├── Elevator 1 (EQUIPMENT)
└── Generator (EQUIPMENT)

1.4 FixedAssetAttribute — EAV cho data đặc thù

RESIDENTIAL_UNIT attributes:
area_sqm, bedrooms, bathrooms, floor_number, direction

COMMERCIAL_SPACE attributes:
area_sqm, floor_number, layout_type (OPEN_PLAN / PRIVATE)

MEETING_ROOM attributes:
capacity, equipment (projector, whiteboard, TV)

PARKING_SLOT attributes:
slot_number, vehicle_type (CAR / MOTORBIKE)

EQUIPMENT attributes:
serial_number, manufacturer, installation_date, last_maintenance_date

1.5 Hierarchy traversal

Dùng adjacency list + materialized path (tương tự OrganizationHierarchy trong nháp):

assetId | parentId | path
────────┼──────────┼────────────────────
bldg-A  | null     | /bldg-A
flr-1   | bldg-A   | /bldg-A/flr-1
u-101   | flr-1    | /bldg-A/flr-1/u-101
u-102   | flr-1    | /bldg-A/flr-1/u-102

Query "tất cả unit thuộc tầng 1": WHERE path LIKE '/bldg-A/flr-1/%'
Query "unit thuộc building A": WHERE path LIKE '/bldg-A/%' AND type = RESIDENTIAL_UNIT

  ---
2. OccupancyAgreement — Aggregate Design

2.1 Core structure

OccupancyAgreement (Aggregate Root)
├── OccupancyAgreementId
├── partyId          → Person hoặc Organization
├── assetId          → FixedAsset (unit / space — không phải floor/building)
├── agreementType    → OWNERSHIP / LEASE
├── status           → PENDING / ACTIVE / TERMINATED / EXPIRED
├── period           → { startDate, endDate (null nếu OWNERSHIP) }
└── contractRef      → nullable, số hợp đồng vật lý tham chiếu

2.2 Các loại agreement

OWNERSHIP:
Person A → owns → Unit 101
endDate = null (không có thời hạn)
Chỉ có 1 ACTIVE OWNERSHIP per unit tại một thời điểm

LEASE (Residential):
Person B → leases → Unit 101
period: { 2025-01-01, 2026-01-01 }

LEASE (Commercial):
Organization Z → leases → Office 1A
period: { 2024-06-01, 2027-06-01 }

2.3 Invariants quan trọng

[I1] Max 1 ACTIVE OWNERSHIP per FixedAsset
[I2] Max 1 ACTIVE LEASE per FixedAsset tại cùng thời điểm
[I3] OWNERSHIP và LEASE có thể đồng thời tồn tại trên cùng 1 unit
→ chủ cho thuê lại: Owner A (OWNERSHIP) + Resident B (LEASE) cùng Unit 101
[I4] LEASE chỉ được tạo trên RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE
→ không lease FLOOR, BUILDING, COMMON_AREA
[I5] OWNERSHIP chỉ được tạo trên RESIDENTIAL_UNIT
→ commercial space thuộc về building owner, không sell riêng lẻ

  ---
3. Lifecycle — OccupancyAgreement kéo theo IAM

Đây là điểm kết nối quan trọng nhất giữa Property domain và IAM:

OccupancyAgreement.status → điều kiện để có RoleContext

OWNERSHIP ACTIVE   → Person có thể có RoleContext { scope=RESIDENT, orgId=unit-101 }
LEASE ACTIVE       → Person/Org có thể có RoleContext { scope=RESIDENT/TENANT, orgId=... }
LEASE TERMINATED   → RoleContext bị revoke

Luồng cụ thể:
[1] BQL tạo OccupancyAgreement(LEASE, Person B, Unit 101, ACTIVE)
[2] Hệ thống (hoặc BQL) tạo RoleContext { scope=RESIDENT, orgId=unit-101 }
cho Person B
[3] Person B có thể login với RESIDENT context

    [4] Lease hết hạn / BQL terminate agreement
    [5] RoleContext bị deactivate
    [6] Person B không còn login được với RESIDENT context

  ---
4. Mô hình hoàn chỉnh — 3 domain kết nối

PARTY DOMAIN                    PROPERTY DOMAIN
─────────────────               ──────────────────────────────
Person ◄──────────────────────► OccupancyAgreement ◄──────► FixedAsset
Organization                      (OWNERSHIP/LEASE)           (hierarchy)
│
│ Employment
▼
Position
(BQL_MANAGER,
TENANT_ADMIN...)
│
│ derives
▼
IAM DOMAIN
─────────────────
RoleContext
{ scope, orgId, roles }

  ---
5. Những gì vẫn cần design riêng (ngoài 3 domain trên)

Domain              Lý do tách riêng
──────────────────  ───────────────────────────────────────────────────
Access Control      AccessCard có lifecycle riêng, gắn Person ↔ Zones
Không phải Party relationship, không phải Agreement

Visitor             Person transient — không cần full Party record
VisitorLog: tên, CMND, host, time window, check-in/out

Booking             Reservation của Facility theo time slot
Gần với Work Effort nhưng semantics khác
(Work Effort = làm việc, Booking = dùng tài nguyên)

Maintenance         Work Effort (Ch.6) — BQL assign Contractor
Work Order          Gắn với Equipment (Fixed Asset type=EQUIPMENT)

  ---
6. Câu hỏi cần quyết định trước khi code

[Q1] OccupancyAgreement nằm trong service nào?
→ Cùng admin service (phase 1) hay tách Property service?

[Q2] FixedAsset hierarchy — ai có quyền setup?
→ UC-O1 (BQL_MANAGER setup cấu trúc tòa nhà)
→ Sau khi Admin cấu hình Building (UC-A2)
→ Building root asset được tạo ở Admin, còn lại ở Operator?

[Q3] Khi OccupancyAgreement terminate, ai trigger deactivate RoleContext?
→ Domain event từ Property domain → IAM domain lắng nghe
→ Hay manual step của BQL?

[Q4] Commercial space: building owner là ai trong model?
→ Building organization (BQL) là implicit owner
→ Hay cần explicit OWNERSHIP record cho commercial spaces?

Câu hỏi nào trong 4 cái này cần giải quyết ngay?
