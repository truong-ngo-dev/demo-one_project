# Property Service — Design

## 1. Trách nhiệm

Quản lý **tài sản vật lý** (tòa nhà, tầng, căn hộ) và **thỏa thuận chiếm dụng** giữa Party với tài sản đó. Là nguồn sự thật cho lifecycle của OccupancyAgreement — khi Agreement thay đổi trạng thái, emit event để các service khác phản ứng.

---

## 2. Domain Model

### 2.1 FixedAsset Hierarchy

```
BUILDING
└── FLOOR
    ├── RESIDENTIAL_UNIT
    ├── COMMERCIAL_SPACE
    ├── COMMON_AREA        — hành lang, sảnh (không assign)
    ├── FACILITY           — tiện ích đặt chỗ (gym, BBQ area)
    ├── MEETING_ROOM       — phòng họp
    └── PARKING_SLOT       — chỗ đỗ xe cụ thể
EQUIPMENT                  — gắn vào bất kỳ node nào (thang máy, máy phát)
```

**Nguyên tắc:**
- `fixed_asset` là 1 bảng duy nhất, self-referencing qua `parent_id`
- `path` (materialized path) tối ưu query cây: `/bldg-A/flr-1/u-101`
- `managing_org_id` chỉ set trên BUILDING — ref đến BQL Organization trong party-service (ID only)
- FixedAsset không có legal identity — không thể ký kết, không phải Party

### 2.2 OccupancyAgreement

OccupancyAgreement là Aggregate Root standalone — không có Agreement supertype.

**Hai loại agreement:**

| Type        | Party                             | Unit                                   | end_date           |
|-------------|-----------------------------------|----------------------------------------|--------------------|
| `OWNERSHIP` | Person                            | RESIDENTIAL_UNIT                       | null (vô thời hạn) |
| `LEASE`     | Person / Household / Organization | RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE | bắt buộc           |

**Invariants:**
- `[I1]` Max 1 ACTIVE OWNERSHIP per FixedAsset
- `[I2]` Max 1 ACTIVE LEASE per FixedAsset tại cùng thời điểm
- `[I3]` OWNERSHIP và LEASE có thể đồng thời tồn tại trên cùng 1 unit (chủ cho thuê lại)
- `[I4]` LEASE chỉ được tạo trên RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE
- `[I5]` OWNERSHIP chỉ được tạo trên RESIDENTIAL_UNIT

### 2.3 Agreement Lifecycle

```
PENDING ──► ACTIVE ──► TERMINATED
                  └──► EXPIRED     (end_date đến mà không gia hạn)
```

Chỉ `→ ACTIVE` và `→ TERMINATED/EXPIRED` mới emit event ảnh hưởng đến RoleContext.

---

## 3. Schema

```sql
CREATE TABLE fixed_asset (
    id              VARCHAR(36) PRIMARY KEY,
    parent_id       VARCHAR(36),
    path            VARCHAR(500) NOT NULL,              -- materialized path: /bldg-A/flr-1/u-101
    type            ENUM('BUILDING', 'FLOOR', 'RESIDENTIAL_UNIT', 'COMMERCIAL_SPACE',
                         'COMMON_AREA', 'FACILITY', 'MEETING_ROOM',
                         'PARKING_SLOT', 'EQUIPMENT') NOT NULL,
    name            VARCHAR(255) NOT NULL,
    code            VARCHAR(50),
    sequence_no     INT DEFAULT 0,                      -- thứ tự trong cùng parent
    status          ENUM('ACTIVE', 'INACTIVE', 'UNDER_MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
    managing_org_id VARCHAR(36),                        -- chỉ set trên BUILDING, ref → party-service
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL,
    FOREIGN KEY (parent_id) REFERENCES fixed_asset(id)
);

CREATE TABLE occupancy_agreement (
    id              VARCHAR(36) PRIMARY KEY,
    party_id        VARCHAR(36) NOT NULL,               -- ref → party-service (Person/Household/Org)
    party_type      ENUM('PERSON', 'HOUSEHOLD', 'ORGANIZATION') NOT NULL,
    asset_id        VARCHAR(36) NOT NULL,               -- ref → fixed_asset (unit/space)
    agreement_type  ENUM('OWNERSHIP', 'LEASE') NOT NULL,
    status          ENUM('PENDING', 'ACTIVE', 'TERMINATED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    start_date      DATE NOT NULL,
    end_date        DATE,                               -- null khi OWNERSHIP
    contract_ref    VARCHAR(100),                       -- số hợp đồng vật lý tham chiếu
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL,
    FOREIGN KEY (asset_id) REFERENCES fixed_asset(id)
);
```

---

## 4. Domain Events Published

| Event                          | Trigger                                    | Payload                                                       |
|--------------------------------|--------------------------------------------|---------------------------------------------------------------|
| `BuildingCreated`              | Tạo Building                               | `{ buildingId, name, managingOrgId }`                         |
| `UnitCreated`                  | Tạo RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE | `{ unitId, type, buildingId, code }`                          |
| `OccupancyAgreementActivated`  | Agreement → ACTIVE                         | `{ agreementId, partyId, partyType, assetId, agreementType }` |
| `OccupancyAgreementTerminated` | Agreement → TERMINATED hoặc EXPIRED        | `{ agreementId, partyId, partyType, assetId, agreementType }` |

**Lý do `agreementType` trong payload:** admin-service cần phân biệt để tạo đúng RoleContext:
- `LEASE` + `PERSON/HOUSEHOLD` → RoleContext `RESIDENT`
- `LEASE` + `ORGANIZATION` → RoleContext `TENANT`
- `OWNERSHIP` + `PERSON` → RoleContext `RESIDENT`

---

## 5. Use Cases

### ADMIN portal — bootstrap

| Use case     | Actor       | Flow                                                               |
|--------------|-------------|--------------------------------------------------------------------|
| Tạo Building | SUPER_ADMIN | Tạo FixedAsset(BUILDING), set managingOrgId → emit BuildingCreated |

### OPERATOR portal — cấu trúc tòa nhà

| Use case                  | Actor       | Flow                                                                                   |
|---------------------------|-------------|----------------------------------------------------------------------------------------|
| Tạo Floor                 | BQL_MANAGER | Tạo FixedAsset(FLOOR), parentId = Building                                             |
| Tạo Unit                  | BQL_MANAGER | Tạo FixedAsset(RESIDENTIAL_UNIT/COMMERCIAL_SPACE), parentId = Floor → emit UnitCreated |
| Tạo Facility/Meeting Room | BQL_MANAGER | Tạo FixedAsset(FACILITY/MEETING_ROOM), parentId = Floor                                |
| Tạo Parking Slot          | BQL_MANAGER | Tạo FixedAsset(PARKING_SLOT), parentId = Floor/Zone                                    |
| Xem cây tài sản           | BQL_MANAGER | Query fixed_asset WHERE path LIKE '/buildingId/%'                                      |

### OPERATOR portal — vòng đời hợp đồng

| Use case                | Actor       | Flow                                                       |
|-------------------------|-------------|------------------------------------------------------------|
| Tạo OccupancyAgreement  | BQL_MANAGER | Tạo OccupancyAgreement(PENDING), validate invariants I1-I5 |
| Activate Agreement      | BQL_MANAGER | status → ACTIVE → emit OccupancyAgreementActivated         |
| Terminate Agreement     | BQL_MANAGER | status → TERMINATED → emit OccupancyAgreementTerminated    |
| Xem hợp đồng theo unit  | BQL_MANAGER | Query occupancy_agreement WHERE asset_id                   |
| Xem hợp đồng theo party | BQL_MANAGER | Query occupancy_agreement WHERE party_id                   |

---

## 6. Business Rules

1. `[I1]` Max 1 ACTIVE OWNERSHIP per FixedAsset
2. `[I2]` Max 1 ACTIVE LEASE per FixedAsset tại cùng thời điểm
3. `[I3]` OWNERSHIP và LEASE có thể đồng thời tồn tại trên cùng 1 unit
4. `[I4]` LEASE chỉ được tạo trên RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE
5. `[I5]` OWNERSHIP chỉ được tạo trên RESIDENTIAL_UNIT
6. OWNERSHIP: `end_date` phải null, `party_type` phải là PERSON
7. LEASE: `end_date` bắt buộc, `party_type` phải match unit type:
   - RESIDENTIAL_UNIT → PERSON hoặc HOUSEHOLD
   - COMMERCIAL_SPACE → ORGANIZATION
8. `managing_org_id` bắt buộc trên BUILDING — không có BQL thì không thể vận hành

---

## 7. Dependency

Property service **không call** sang service khác. Mọi cross-service data đều là reference ID.

Các service khác consume events từ property-service:
- `admin-service` → lắng nghe `OccupancyAgreementActivated/Terminated` để manage RoleContext
- `admin-service` → lắng nghe `BuildingCreated` để cache building reference
