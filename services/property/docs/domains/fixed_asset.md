# Domain: FixedAsset

## Mô tả

Aggregate Root đại diện cho **tài sản vật lý** trong hệ thống — tòa nhà, tầng, căn hộ, tiện ích, thiết bị. Tổ chức dạng cây self-referencing qua `parentId`. Không có legal identity — không ký kết, không phải Party.

---

## Trách nhiệm
- Lưu giữ thông tin vật lý của tài sản (loại, tên, mã, trạng thái, vị trí trong cây).
- Quản lý trạng thái vận hành (ACTIVE, INACTIVE, UNDER_MAINTENANCE).
- Là anchor cho OccupancyAgreement — Agreement gắn vào FixedAsset.

## Không thuộc trách nhiệm
- Không biết về OccupancyAgreement hay ai đang chiếm dụng.
- Không biết về Party, User, RoleContext.
- Không validate invariants của Agreement (thuộc OccupancyAgreement domain).

---

## Cấu trúc Aggregate

```
FixedAsset
├── FixedAssetId                (Value Object — typed UUID)
├── FixedAssetType              (enum: BUILDING | FLOOR | RESIDENTIAL_UNIT | COMMERCIAL_SPACE |
│                                       COMMON_AREA | FACILITY | MEETING_ROOM | PARKING_SLOT | EQUIPMENT)
├── name                        (String)
├── code                        (String — nullable, mã căn hộ / phòng)
├── sequenceNo                  (int — thứ tự trong cùng parent, default 0)
├── parentId                    (FixedAssetId — nullable, chỉ BUILDING mới null)
├── path                        (String — materialized path: /buildingId/floorId/unitId)
├── FixedAssetStatus            (enum: ACTIVE | INACTIVE | UNDER_MAINTENANCE)
├── managingOrgId               (String — nullable, chỉ set trên BUILDING, ref → party-service)
├── createdAt                   (Instant — immutable)
└── updatedAt                   (Instant)
```

---

## FixedAsset Hierarchy

```
BUILDING (parentId = null)
└── FLOOR
    ├── RESIDENTIAL_UNIT
    ├── COMMERCIAL_SPACE
    ├── COMMON_AREA        — hành lang, sảnh (không assign agreement)
    ├── FACILITY           — gym, BBQ area
    ├── MEETING_ROOM
    └── PARKING_SLOT
EQUIPMENT                  — gắn vào bất kỳ node nào (thang máy, máy phát)
```

---

## Materialized Path

`path` là chuỗi `/id1/id2/id3` tính tại application layer khi tạo:
- BUILDING: `/{assetId}`
- FLOOR: `/{buildingId}/{assetId}`
- Các loại khác: `{parentPath}/{assetId}`

Query cây: `WHERE path LIKE '/{buildingId}%'`

---

## Trạng thái (FixedAssetStatus)

```
ACTIVE             → đang hoạt động bình thường
UNDER_MAINTENANCE  → tạm ngưng (bảo trì, sửa chữa)
INACTIVE           → ngưng vận hành vĩnh viễn (không xoá)
```

---

## Hành vi

| Hành vi               | Điều kiện           | Mô tả                              |
|-----------------------|---------------------|------------------------------------|
| `create()`            | managingOrgId bắt buộc nếu type=BUILDING (I8) | Factory |
| `deactivate()`        | status != INACTIVE  | Chuyển sang INACTIVE               |
| `setUnderMaintenance()` | status = ACTIVE   | Chuyển sang UNDER_MAINTENANCE      |
| `reactivate()`        | status != ACTIVE    | Chuyển về ACTIVE                   |

---

## Invariants

- `FixedAssetId` immutable sau khi tạo.
- `FixedAssetType` immutable sau khi tạo.
- `[I8]` BUILDING bắt buộc có `managingOrgId` — validate tại `create()`.
- `managingOrgId` chỉ được set trên BUILDING — không validate reference tồn tại (reference ID only).
- `path` immutable sau khi tạo (tree structure không thay đổi).

---

## Events

| Event                 | Trigger              | Consumer                          |
|-----------------------|----------------------|-----------------------------------|
| `BuildingCreatedEvent` | Tạo BUILDING        | admin-service (cache building ref) |
| `UnitCreatedEvent`    | Tạo RESIDENTIAL_UNIT / COMMERCIAL_SPACE | — (Phase 1) |

---

## Error Codes

| Code                          | HTTP | Mô tả                                                   |
|-------------------------------|------|---------------------------------------------------------|
| `ASSET_NOT_FOUND`             | 404  | Không tìm thấy FixedAsset với id đã cho                 |
| `ASSET_ALREADY_INACTIVE`      | 422  | Asset đã ở trạng thái INACTIVE                          |
| `MANAGING_ORG_REQUIRED`       | 422  | Building phải có managingOrgId                          |
| `INVALID_ASSET_TYPE_FOR_PARENT` | 422 | Loại asset không hợp lệ với parent                     |

---

## Tham khảo
- [Property Service Design](../../../../docs/development/260416_01_design_party_model/02_property_service.md)
- [Implementation Plan](../../../../docs/development/260416_01_design_party_model/property_service_plan.md)
