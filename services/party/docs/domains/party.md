# Domain: Party

## Mô tả

Aggregate Root cốt lõi — đại diện cho **bất kỳ tác nhân nào** có thể tham gia vào hệ thống. `Party` lưu core identity (type, name, status) và sở hữu lifecycle của `PartyIdentification`. Mỗi Person, Organization, Household đều có một `Party` record tương ứng với cùng ID.

---

## Trách nhiệm
- Lưu giữ core identity của tác nhân (type, name, status).
- Quản lý thông tin định danh pháp lý (`PartyIdentification`).
- Kiểm soát trạng thái hoạt động của tác nhân.

## Không thuộc trách nhiệm
- Không biết về subtype-specific data (họ tên, orgType, v.v.) — thuộc về Person/Organization/Household AR riêng.
- Không biết về relationship giữa các Party — thuộc về `PartyRelationship` aggregate.
- Không biết về FixedAsset, RoleContext, hay Agreement.

---

## Cấu trúc Aggregate

```
Party
├── PartyId                        (Value Object — typed UUID)
├── PartyType                      (enum: PERSON, ORGANIZATION, HOUSEHOLD)
├── name                           (String — display name)
├── PartyStatus                    (enum: ACTIVE, INACTIVE)
├── List<PartyIdentification>      (Entity — owned by Party)
├── createdAt                      (Instant — immutable)
└── updatedAt                      (Instant — cập nhật mỗi khi aggregate thay đổi)
```

> **Composition + Shared ID:** `Party` luôn được tạo trước subtype. Person/Organization/Household AR share cùng `PartyId`. Tạo Party + subtype phải atomic tại **application layer** (cùng transaction).

---

## Value Objects

### PartyId
- Typed UUID — không dùng raw `String` hay `UUID` trong domain.

---

## Entity: PartyIdentification

Thông tin định danh pháp lý của Party. Một Party có thể có nhiều loại định danh (CCCD, hộ chiếu, mã số thuế, ...).

```
PartyIdentification
├── id             (UUID — generated)
├── partyId        (PartyId — back-reference)
├── type           (PartyIdentificationType — CCCD | TAX_ID | PASSPORT | BUSINESS_REG)
├── value          (String — mã số, số giấy tờ)
└── issuedDate     (LocalDate — optional)
```

**Invariants:**
- `(type, value)` unique toàn hệ thống — không có 2 Party có cùng loại + số giấy tờ.
- `value` immutable sau khi tạo — nếu sai phải xoá và thêm mới.

---

## Trạng thái (PartyStatus)

```
ACTIVE    → tác nhân đang hoạt động bình thường
INACTIVE  → tác nhân không còn hoạt động (off-board, không xoá để giữ audit trail)
```

### Transition rules

```
ACTIVE → INACTIVE   — deactivate (BQL_MANAGER)
INACTIVE → ACTIVE   — reactivate (BQL_MANAGER) [PLANNED]
```

---

## Hành vi

### Identification Management

| Hành vi                   | Điều kiện              | Mô tả                                              |
|---------------------------|------------------------|----------------------------------------------------|
| `addIdentification`       | status = ACTIVE        | Thêm PartyIdentification mới; check unique (type, value) |
| `removeIdentification`    | identification tồn tại | Xoá PartyIdentification theo id                    |

### Status

| Hành vi        | Điều kiện         | Mô tả                                      |
|----------------|-------------------|--------------------------------------------|
| `deactivate`   | status = ACTIVE   | Chuyển Party sang INACTIVE                 |

---

## Invariants
- `PartyId` immutable sau khi tạo.
- `PartyType` immutable sau khi tạo — không chuyển Person thành Organization.
- `(PartyIdentificationType, value)` unique toàn hệ thống.
- Party không thể bị xoá — chỉ deactivate để giữ audit trail.

---

## Events

| Event                    | Trigger                         | Handler (consumer)                     |
|--------------------------|---------------------------------|----------------------------------------|
| `PersonCreatedEvent`     | Tạo Person (tạo Party+Person)   | — (Phase 1, chưa có consumer)          |
| `OrganizationCreatedEvent` | Tạo Organization              | admin-service (cache BQL org reference) |
| `HouseholdCreatedEvent`  | Tạo Household                   | — (Phase 1, chưa có consumer)          |

> Events được phát từ **application layer** sau khi tạo Party + subtype atomic.

---

## Error Codes

| Code                              | HTTP | Mô tả                                                   |
|-----------------------------------|------|---------------------------------------------------------|
| `PARTY_NOT_FOUND`                 | 404  | Không tìm thấy Party với id đã cho                      |
| `PARTY_ALREADY_INACTIVE`          | 422  | Party đã ở trạng thái INACTIVE                          |
| `IDENTIFICATION_ALREADY_EXISTS`   | 409  | (type, value) đã tồn tại cho Party khác                 |
| `IDENTIFICATION_NOT_FOUND`        | 404  | Không tìm thấy PartyIdentification với id đã cho        |
| `INVALID_PARTY_STATUS`            | 422  | Thao tác không hợp lệ với status hiện tại               |

---

## Quan hệ

| Domain          | Quan hệ                                                                                |
|-----------------|----------------------------------------------------------------------------------------|
| `Person`        | Share `PartyId` — Person AR created atomic cùng Party AR                               |
| `Organization`  | Share `PartyId` — Organization AR created atomic cùng Party AR                         |
| `Household`     | Share `PartyId` — Household AR created atomic cùng Party AR                            |
| `PartyRelationship` | Reference `PartyId` — Party không biết về relationship                            |

---

## Tham khảo
- [Party Service Design](../../../../docs/development/260416_01_design_party_model/01_party_service.md)
- [Implementation Plan](../../../../docs/development/260416_01_design_party_model/plan.md)
