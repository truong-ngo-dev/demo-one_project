Party Model + Property Model — Mapping cho BMS                                                                                                                                                                  
Bắt đầu từ nguyên tắc: mỗi actor và operation phải tìm được "chỗ ngồi" trong model. Nếu không tìm được   → đó là gap cần extend.                                                                                                                                                                                         
---                                                                                                      1. Mapping Actors → Party Model

Tất cả "người" → Person (Party)

IT Admin (SUPER_ADMIN)     → Person
BQL_MANAGER                → Person
BQL_STAFF / FINANCE / ...  → Person
TENANT_ADMIN               → Person
TENANT_EMPLOYEE            → Person
Unit Owner                 → Person
Resident (lessee)          → Person
Visitor                    → Person
Contractor employee        → Person

Tất cả "tổ chức" → Organization (Party)

BQL company                → Organization (type: BUILDING_MANAGEMENT)
Tenant company (Company Z) → Organization (type: TENANT)
Contractor company         → Organization (type: VENDOR)

  ---
2. Mapping Relationships → Party Model

Employment (Ch.9) — Người làm việc cho tổ chức

BQL staff     → Employment → BQL Organization
Position: BQL_MANAGER / BQL_FINANCE / BQL_TECHNICAL / ...

Tenant staff  → Employment → Tenant Organization
Position: chức vụ nội bộ (Director, Manager, Staff...)

Contractor    → Employment → Contractor Organization

PartyRelationship (Ch.2) — Quan hệ giữa các Party

Organization (BQL) ←→ Organization (Tenant)
RelationshipType: LANDLORD_TENANT

Organization (BQL) ←→ Organization (Contractor)
RelationshipType: CLIENT_VENDOR

Person (Visitor) ←→ Person (Host) hoặc Organization (Tenant)
RelationshipType: VISITOR_OF
Period: { from: ngày giờ đến, to: ngày giờ ra }

  ---
3. Mapping Physical Space → Fixed Asset (Ch.6)

Đây là phần ngoài Party Model — dùng Fixed Asset từ Chapter 6:

Fixed Asset hierarchy (tree):

Building (type: BUILDING)
└── Floor 1 (type: FLOOR)
│    ├── Unit 101 (type: RESIDENTIAL_UNIT)
│    ├── Unit 102 (type: RESIDENTIAL_UNIT)
│    └── Office A (type: COMMERCIAL_SPACE)
└── Floor 2 ...
└── Common Facilities
├── Meeting Room A (type: FACILITY)
├── Gym            (type: FACILITY)
└── Parking Zone   (type: FACILITY)

Fixed Asset Type là catalogue — thêm type mới không cần thay đổi schema.

  ---
4. Connecting Party ↔ Fixed Asset — Gap lớn nhất

Silverston có Party Fixed Asset Assignment (p.221) — nhưng đây là "ai chịu trách nhiệm quản lý tài     
sản", không phải "ai đang sống/làm việc tại đây". BMS cần thêm:

Silverston có sẵn:                   BMS cần extend:
──────────────────────────────────   ──────────────────────────────────
Party Fixed Asset Assignment         Occupancy Agreement
(ai manage tài sản)                  (ai đang CHIẾM DỤNG không gian)

                                       ┌───────────────────────────────────┐
                                       │ OccupancyAgreement                │
                                       │   partyId     → Person / Org      │
                                       │   assetId     → Fixed Asset       │
                                       │   type        → LEASE / OWNERSHIP │
                                       │   period      → from / to         │
                                       │   status      → ACTIVE / ENDED    │
                                       └───────────────────────────────────┘

Các loại OccupancyAgreement:

Unit Owner:
Person (A) → OccupancyAgreement(OWNERSHIP) → Unit 101
Không có ngày kết thúc (hoặc khi bán)

Resident (lessee):
Person (B) → OccupancyAgreement(LEASE) → Unit 101
Period: { from: 2025-01-01, to: 2026-01-01 }

Tenant org:
Organization (Z) → OccupancyAgreement(LEASE) → Office A
Period: { from: 2024-06-01, to: 2027-06-01 }

Trường hợp chủ cho thuê lại: 2 OccupancyAgreement tồn tại song song trên cùng 1 unit — không conflict.

  ---
5. Điều quan trọng — PartyRole ≠ IAM RoleContext

PartyRole (Party Model):             RoleContext (IAM):
────────────────────────────────     ────────────────────────────────────
UNIT_OWNER                           scope=RESIDENT, roles=[RESIDENT]
RESIDENT                             scope=OPERATOR, roles=[BQL_MANAGER]
TENANT                               scope=TENANT, roles=[TENANT_ADMIN]
CONTRACTOR

"Tôi là ai trong nghiệp vụ"          "Tôi được làm gì trong hệ thống"
Stable, gắn với thực tế              Thay đổi theo context/session
Không liên quan đến IT               Concern của IAM layer

IAM đọc từ Party Model để quyết định RoleContext khả dụng — nhưng không replace nó.

  ---
6. Toàn bộ model map

┌─────────────────────────────────────────────────────────────────┐
│  PARTY DOMAIN (Ch.2)                                            │
│  Person ──────────────────── Organization                       │
│    │                              │                             │
│    ├── PartyRole                  ├── PartyRole                 │
│    ├── PartyAttribute             ├── PartyAttribute            │
│    └── PartyRelationship ─────────┘                             │
└──────────────────┬──────────────────────┬───────────────────────┘
                   │                      │
       ┌───────────▼──────────┐  ┌────────▼────────────────────┐
       │  HR DOMAIN (Ch.9)    │  │  PROPERTY DOMAIN (Ch.6 ext) │
       │  Employment          │  │  Fixed Asset (hierarchy)    │
       │  Position            │  │  Fixed Asset Type           │
       │  PositionAssignment  │  │  OccupancyAgreement (ext)   │
       └──────────────────────┘  └─────────────────────────────┘
                                           │
                              ┌────────────▼───────────────────┐
                              │  IAM DOMAIN                    │
                              │  User (linked to Person)       │
                              │  RoleContext (derived)         │
                              └────────────────────────────────┘

  ---
7. Gap summary — Những gì BMS cần tự design ngoài Silverston

OccupancyAgreement      → Party ↔ Fixed Asset với ngữ nghĩa occupancy
Access Card             → không có trong Silverston, domain riêng
Visitor record          → Person (transient) + time-bound relationship
Facility Booking        → reservation system, không phải work effort
Contractor Work Order   → gần với Work Effort (Ch.6) nhưng cần adapt
