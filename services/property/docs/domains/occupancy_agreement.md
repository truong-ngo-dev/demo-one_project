# Domain: OccupancyAgreement

## Mô tả

Aggregate Root standalone đại diện cho **thỏa thuận chiếm dụng** giữa một Party và một FixedAsset. Là nguồn sự thật về ai đang sở hữu/thuê căn hộ nào. Khi status thay đổi sang ACTIVE hoặc TERMINATED/EXPIRED, emit event để admin-service cập nhật RoleContext.

---

## Trách nhiệm
- Quản lý lifecycle của agreement (PENDING → ACTIVE → TERMINATED/EXPIRED).
- Enforce invariants I4–I7 tại domain layer.
- Emit domain events khi trạng thái thay đổi có ảnh hưởng đến hệ thống.

## Không thuộc trách nhiệm
- Không enforce I1/I2 (cần query repo — thuộc application layer).
- Không biết về Party internals — chỉ lưu reference ID.
- Không biết về RoleContext hay User.

---

## Cấu trúc Aggregate

```
OccupancyAgreement
├── OccupancyAgreementId            (Value Object — typed UUID)
├── partyId                         (String — ref → party-service)
├── PartyType                       (enum local: PERSON | HOUSEHOLD | ORGANIZATION)
├── assetId                         (String — ref → fixed_asset)
├── OccupancyAgreementType          (enum: OWNERSHIP | LEASE)
├── OccupancyAgreementStatus        (enum: PENDING | ACTIVE | TERMINATED | EXPIRED)
├── startDate                       (LocalDate)
├── endDate                         (LocalDate — null khi OWNERSHIP)
├── contractRef                     (String — nullable, số hợp đồng vật lý)
├── createdAt                       (Instant — immutable)
└── updatedAt                       (Instant)
```

> `PartyType` là enum local của property-service — **không import từ party-service**.

---

## Hai loại Agreement

| Type        | partyType hợp lệ                  | assetType hợp lệ                       | endDate           |
|-------------|-----------------------------------|----------------------------------------|-------------------|
| `OWNERSHIP` | PERSON                            | RESIDENTIAL_UNIT                       | null (vô thời hạn)|
| `LEASE`     | PERSON / HOUSEHOLD / ORGANIZATION | RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE | bắt buộc          |

---

## Agreement Lifecycle

```
PENDING ──► ACTIVE ──► TERMINATED  (manual)
                  └──► EXPIRED     (end_date đến)
```

**Chỉ `→ ACTIVE` và `→ TERMINATED/EXPIRED`** emit event.

---

## Invariants

| ID  | Layer       | Rule                                                                                              |
|-----|-------------|---------------------------------------------------------------------------------------------------|
| I1  | Application | Max 1 ACTIVE OWNERSHIP per FixedAsset                                                             |
| I2  | Application | Max 1 ACTIVE LEASE per FixedAsset tại cùng thời điểm                                             |
| I3  | —           | OWNERSHIP và LEASE có thể đồng thời (tự nhiên từ I1 + I2)                                        |
| I4  | Domain      | LEASE chỉ được tạo trên RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE                                   |
| I5  | Domain      | OWNERSHIP chỉ được tạo trên RESIDENTIAL_UNIT                                                     |
| I6  | Domain      | OWNERSHIP: endDate phải null; partyType phải là PERSON                                            |
| I7  | Domain      | LEASE: endDate bắt buộc; RESIDENTIAL_UNIT → PERSON/HOUSEHOLD; COMMERCIAL_SPACE → ORGANIZATION   |

**Cách validate I4/I5/I7 tại domain:** `create()` nhận `FixedAssetType assetType` làm param (không lưu vào aggregate) — application layer load FixedAsset trước rồi pass vào.

---

## Hành vi

| Hành vi       | Transition               | Điều kiện           | Ghi chú                          |
|---------------|--------------------------|---------------------|----------------------------------|
| `create()`    | — → PENDING              | I4/I5/I6/I7 pass    | Nhận assetType để validate       |
| `activate()`  | PENDING → ACTIVE         | status = PENDING    | Raise ActivatedEvent             |
| `terminate()` | ACTIVE → TERMINATED      | status = ACTIVE     | Raise TerminatedEvent            |
| `expire()`    | ACTIVE → EXPIRED         | status = ACTIVE     | Raise TerminatedEvent (same)     |

---

## Events

| Event                               | Trigger                   | Payload                                                         | Consumer         |
|-------------------------------------|---------------------------|-----------------------------------------------------------------|------------------|
| `OccupancyAgreementActivatedEvent`  | `activate()`              | `{ agreementId, partyId, partyType, assetId, agreementType }`   | admin-service    |
| `OccupancyAgreementTerminatedEvent` | `terminate()` / `expire()`| `{ agreementId, partyId, partyType, assetId, agreementType }`   | admin-service    |

**`agreementType` bắt buộc trong payload** — admin-service dùng để phân biệt:
- `LEASE` + `PERSON/HOUSEHOLD` → RoleContext `RESIDENT`
- `LEASE` + `ORGANIZATION` → RoleContext `TENANT`
- `OWNERSHIP` + `PERSON` → RoleContext `RESIDENT`

---

## Error Codes

| Code                              | HTTP | Mô tả                                                         |
|-----------------------------------|------|---------------------------------------------------------------|
| `AGREEMENT_NOT_FOUND`             | 404  | Không tìm thấy Agreement với id đã cho                        |
| `AGREEMENT_INVALID_STATUS`        | 422  | Transition không hợp lệ từ status hiện tại                    |
| `OWNERSHIP_ALREADY_EXISTS`        | 409  | Đã có ACTIVE OWNERSHIP trên asset này (I1)                    |
| `LEASE_ALREADY_EXISTS`            | 409  | Đã có ACTIVE LEASE trên asset này (I2)                        |
| `INVALID_ASSET_TYPE_FOR_LEASE`    | 422  | LEASE chỉ hợp lệ với RESIDENTIAL_UNIT / COMMERCIAL_SPACE (I4) |
| `INVALID_ASSET_TYPE_FOR_OWNERSHIP`| 422  | OWNERSHIP chỉ hợp lệ với RESIDENTIAL_UNIT (I5)               |
| `INVALID_PARTY_TYPE_FOR_OWNERSHIP`| 422  | OWNERSHIP chỉ dành cho PERSON (I6)                            |
| `INVALID_PARTY_TYPE_FOR_UNIT`     | 422  | partyType không khớp với loại unit (I7)                       |
| `END_DATE_REQUIRED_FOR_LEASE`     | 422  | LEASE phải có endDate (I7)                                    |

---

## Tham khảo
- [Property Service Design](../../../../docs/development/260416_01_design_party_model/02_property_service.md)
- [Implementation Plan](../../../../docs/development/260416_01_design_party_model/property_service_plan.md)
